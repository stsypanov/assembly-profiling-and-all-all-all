package com.tsypanov.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class HasIdAndNameDto implements HasIdAndName {
	
	private final Long id;
	private final String name;

}
