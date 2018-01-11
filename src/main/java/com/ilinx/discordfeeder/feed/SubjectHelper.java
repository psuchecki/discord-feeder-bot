package com.ilinx.discordfeeder.feed;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SubjectHelper {

	public static final String FIRST_REGEX = "\\[FIRST\\]";
	public static final Pattern FIRST_REGEX_PATTERN = Pattern.compile(FIRST_REGEX);
	public static final String NORMAL_TAG = "INFO - ";
	public static final String FIRST_TAG = "FIRST - ";
	public static final String FIRST_IMAGE_TAG = "IMAGE-FIRST - ";

	public String createSubject(String subjectConst, String content, String createTime, String imageUrl) {
		String resultSubject;
		Matcher matcher = FIRST_REGEX_PATTERN.matcher(content.toUpperCase());
		if (matcher.find()) {
			resultSubject = FIRST_TAG + subjectConst + getDateWithoutTime(createTime);
		} else if (!StringUtils.isEmpty(imageUrl)) {
			resultSubject = FIRST_IMAGE_TAG + subjectConst + getDateWithoutTime(createTime);
		} else {
			resultSubject = NORMAL_TAG + subjectConst + getDateWithoutTime(createTime);
		}

		return resultSubject;
	}

	private String getDateWithoutTime(String createTime) {
		return createTime.substring(0, createTime.indexOf("T"));
	}
}
