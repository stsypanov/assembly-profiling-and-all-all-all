**Примеры для доклада по низкоуровневому профилированию на SnowOne/JPoint**

План доклада:

0. Предыстория. Даже если вы читали статью [Ускоряем создание ConcurrentReferenceHashMap](https://habr.com/ru/post/432824/)
   доклад будет для вас интересным.
   Настройка окружения описана в статье [Профилирование с точностью до ассемблерных инструкций](https://habr.com/ru/post/542148/)
   Настройка asyn-profiler-а описана [на странице проекта](https://github.com/jvm-profiling-tools/async-profiler)
   Настройка asyn-profiler-а описана [на странице проекта](https://github.com/jvm-profiling-tools/async-profiler)
   Настройка asyn-profiler-а описана [на странице проекта](https://github.com/jvm-profiling-tools/async-profiler)

1. Постановка задачи. Запуск `ProjectionVsDtoBenchmark` показывает существенную разницу в производительности
   между выгрузкой данных с помощью DTO и проекций:
```
Benchmark                          (count)  Mode  Cnt       Score       Error   Units
dto                                      1  avgt   50      11,409 ±     0,741   us/op
projection                               1  avgt   50      12,886 ±     0,194   us/op

dto                                    100  avgt   50      40,326 ±     0,738   us/op
projection                             100  avgt   50     220,189 ±     5,668   us/op

dto:·gc.alloc.rate.norm                  1  avgt   50   13994,528 ±    22,042    B/op
projection:·gc.alloc.rate.norm           1  avgt   50   21224,235 ±    19,369    B/op

dto:·gc.alloc.rate.norm                100  avgt   50   33313,019 ±    22,609    B/op
projection:·gc.alloc.rate.norm         100  avgt   50  517354,738 ±    34,719    B/op
```

2. Запуск теста `ManyFieldsRepositoryTest` с остановкой на бряке в последней строчке
   и исследование внутренностей двух полученных объектов. Оказывается, что проекция устроена намного сложнее и
   основана на прокси.

3. Профилирование `ProjectionProfilingBenchmark` для определения горячих мест в коде.
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
   Проверяем вывод `AsyncProfiler`-а. Картина примерно та же.
```
        ns  percent  samples  top
----------  -------  -------  ---
4270110916   27.81%      427  o.s.u.ConcurrentReferenceHashMap$Segment.<init>
3920827445   25.53%      392  o.s.u.ConcurrentReferenceHashMap$ReferenceManager.<init>
3691037002   24.04%      369  o.s.u.ConcurrentReferenceHashMap.createReferenceManager
1719959854   11.20%      172  o.s.u.ConcurrentReferenceHashMap.<init>
 439959922    2.87%       44  java.lang.ref.ReferenceQueue.<init>
 279927288    1.82%       28  java.util.concurrent.locks.ReentrantLock.<init>
  70590822    0.46%        7  smp_call_function_single_[k]
  ```

6. Поскольку использование `AsyncProfiler`-а не даёт требуемой точности, то воспользуемся `LinuxPerfAsmProfiler`-ом
   (`DTraceAsmProfiler` для MacOS)
```shell
java -jar target/benchmarks.jar CRHMBenchmark -f 2 -w 1s -r 1s -prof perfasm
```

7. Объясняем смысл полученных данных, горячие места и исправления.

8. Рассказываем про практические выводы после первой части:
 - опасность `volatile` в цикле, читающем/пишущем в поле
 - ненужность и вредность явного обнуления волатильных полей и объектов атомарных классов при их создании
 - примеры:
  https://github.com/spring-projects/spring-framework/pull/25261
  https://github.com/spring-projects/spring-framework/pull/25846
  https://github.com/openjdk/jdk/pull/5197
  https://github.com/openjdk/jdk/pull/510

```
Benchmark                                     Score   Error  Units

VolatileFieldZeroingBenchmark.explicitInit   11.200 ± 0.270  ns/op
VolatileFieldZeroingBenchmark.defultInit      3.447 ± 0.208  ns/op

AtomicZeroingBenchmark.defaultValue           3,037 ± 0,100  ns/op
AtomicZeroingBenchmark.explicitValue         13,976 ± 0,220  ns/op
``` 

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
    обёртки создаются и отравляют профиль. Обратите внимание, что это работает только с новыми версиями Java (14 и старше).
```shell
java -jar target/benchmarks.jar TurnOffInliningBenchmark -f 2 -w 1s -r 1s -prof gc
```
Итог таков:
```
Benchmark                                                                          Score    Error   Units

TurnOffInliningBenchmark.calculate                                                51,099 ±  0,842   ns/op
TurnOffInliningBenchmark.calculate:·gc.alloc.rate                                  0,001 ±  0,001  MB/sec
TurnOffInliningBenchmark.calculate:·gc.alloc.rate.norm                            ≈ 10⁻⁴             B/op
TurnOffInliningBenchmark.calculate:·gc.count                                         ≈ 0           counts

TurnOffInliningBenchmark.calculateNoInlining                                     766,289 ± 17,697   ns/op
TurnOffInliningBenchmark.calculateNoInlining:·gc.alloc.rate                     2443,419 ± 54,399  MB/sec
TurnOffInliningBenchmark.calculateNoInlining:·gc.alloc.rate.norm                2944,301 ±  0,014    B/op
TurnOffInliningBenchmark.calculateNoInlining:·gc.churn.G1_Eden_Space            2437,723 ± 70,539  MB/sec
TurnOffInliningBenchmark.calculateNoInlining:·gc.churn.G1_Eden_Space.norm       2937,362 ± 52,498    B/op
TurnOffInliningBenchmark.calculateNoInlining:·gc.churn.G1_Survivor_Space           0,015 ±  0,002  MB/sec
TurnOffInliningBenchmark.calculateNoInlining:·gc.churn.G1_Survivor_Space.norm      0,018 ±  0,003    B/op
TurnOffInliningBenchmark.calculateNoInlining:·gc.count                           596,000           counts
TurnOffInliningBenchmark.calculateNoInlining:·gc.time                            469,000               ms
```
Таким образом, использование `-XX:MaxInlineLevel=0` даёт неожиданный эффект: в коде появляются ложные горячие места,
которых в обычном режиме просто нет.

13. Итоговые выводы
 - ручное зануление волатильных полей не только избыточно, но и вредно для производительности
 - пользуйтесь флагами виртуальной машины при прогонах, они помогут вам сузить область поиска 
 - выполняйте множество прогонов и собирайте статистику
