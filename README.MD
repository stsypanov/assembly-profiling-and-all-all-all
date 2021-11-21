**Примеры для доклада по низкоуровневому профилированию на SnowOne/JPoint**

План самого доклада:

0. Предыстория. Даже если вы читали [https://habr.com/ru/post/432824/](https://habr.com/ru/post/432824/)
   доклад будет для вас интересным.
   Настройка окружения описана в [https://habr.com/ru/post/542148/](https://habr.com/ru/post/542148/)

1. Постановка задачи. Запуск `ProjectionVsDtoBenchmark` показывает существенную разницу в производительности
   между выгрузкой данных с помощью DTO и проекций:
```
Benchmark                                            (count)  Mode  Cnt       Score       Error   Units
findAllByName                                              1  avgt   50      12,886 ±     0,194   us/op
findAllByNameUsingDto                                      1  avgt   50      11,409 ±     0,741   us/op

findAllByName                                            100  avgt   50     220,189 ±     5,668   us/op
findAllByNameUsingDto                                    100  avgt   50      40,326 ±     0,738   us/op

findAllByName:·gc.alloc.rate.norm                          1  avgt   50   21224,235 ±    19,369    B/op
findAllByNameUsingDto:·gc.alloc.rate.norm                  1  avgt   50   13994,528 ±    22,042    B/op

findAllByName:·gc.alloc.rate.norm                        100  avgt   50  517354,738 ±    34,719    B/op
findAllByNameUsingDto:·gc.alloc.rate.norm                100  avgt   50   33313,019 ±    22,609    B/op
```

2. Запуск теста `ManyFieldsRepositoryTest` с остановкой на бряке в последней строчке
   и исследование внутренностей двух полученных объектов.

3. Профилирование `ProjectionProfilingBenchmark` для  определения горячих мест в коде.
   Профилирование выполняется с помощью `JavaFlightRecorderProfiler`. Обнаруживаем, что горячим местом является создание
   `org.springframework.util.ConcurrentReferenceHashMap`

5. Запуск `CRHMBenchmark` одноврменно с `JavaFlightRecorderProfiler`-ом и `AsyncProfiler`-ом для более точного профилирования:
```shell
java -jar -Djava.library.path=/Users/stsypanov/IdeaProjects/async-profiler/build/ target/benchmarks.jar CRHMBenchmark -f 2 -w 1s -r 1s -prof async
```
   Перед запуском `AsyncProfiler`-а нужно разрешить профилировщику доступ к событиям на уровне ядра с помощью
```shell
$ sudo sysctl kernel.perf_event_paranoid=1
$ sudo sysctl kernel.kptr_restrict=0
```
   Также необходимо убедиться, что установлены отладочные символы (`apt install openjdk-8-dbg`/`apt install openjdk-11-dbg`).
   Сначала проверяем вывод `JavaFlightRecorderProfiler`-а и обнаруживаем, что он не даёт нужной нам точности.
   Проверяем вывод `AsyncProfiler`-а.

6. Поскольку использование `AsyncProfiler`-а не даёт требуемой точности, то воспользуемся `LinuxPerfAsmProfiler`-ом
   (`DTraceAsmProfiler` для MacOS)
```shell
java -jar target/benchmarks.jar CRHMBenchmark -f 2 -w 1s -r 1s -prof perfasm
```

7. Объясняем смысл полученых данных, горячие места и исправления.

8. Рассказываем про практические выводы после первой части:
 - опасность `volatile` в цикле, читающем/пишущем в поле
 - ненужность и вредность явного обнуления волатильных полей и объектов атомарных классов при их создании
 - примеры:
  https://github.com/spring-projects/spring-framework/pull/25261
  https://github.com/spring-projects/spring-framework/pull/25846
  https://github.com/openjdk/jdk/pull/5197

10. Запускаем `CRHMBenchmark` на Java 11 или выше. Приходим к выводу, что для одного из горячих мест отсутствует
    ссылка на исполняемый байт-код. 
    Собрав статистику 100 прогонов, обнаруживаем, что лишь в 23 прогонах из 100 указан нужный байт-код!
    Задаём вопрос в сообществе:
    https://stackoverflow.com/questions/63397711/linuxperfasmprofiler-shows-java-code-corresponding-assembly-hot-spot-for-java-8
    Андрей Паньгин подтверждает, что речь действительно идёт о баге в ВМ.
    Пишем в рассылку:
    https://mail.openjdk.java.net/pipermail/hotspot-compiler-dev/2020-August/039512.html
    Ответ в духе Дмитрия нашего Анатольевича:
    https://mail.openjdk.java.net/pipermail/hotspot-compiler-dev/2020-August/039513.html

12. Проверяем для Java 17. Вывод: начиная с "девятки" сопоставление инструкций ассемблера и байт-кода сломано и чинить
    его никто не собирается.

13. Андрей Паньгин предлагает элегантное решение: отключаем вклеивание с помощью `-XX:MaxInlineLevel=0`.
    Это действительно решает проблему, но может существенно исказить профиль, что показывает запуск `TurnOffInliningBenchmark`.
    Если при обычно прогоне все аллокации стираются, а горячим местом является инкремент, то при запуске с `-XX:MaxInlineLevel=0`
    обёртки создаются и отравляют профиль.
```shell
java -jar target/benchmarks.jar TurnOffInliningBenchmark -f 2 -w 1s -r 1s -prof gc
```

13. Итоговые выводы