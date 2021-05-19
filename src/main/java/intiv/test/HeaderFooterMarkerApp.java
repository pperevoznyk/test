package com.test.demo;

import com.test.demo.service.HeaderFooterService;
import com.test.demo.service.TsvReaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class HeaderFooterMarkerApp {

	private final TsvReaderService tsvReaderService;
	private final HeaderFooterService headerFooterService;

	public static void main(String[] args) {
		SpringApplication.run(HeaderFooterMarkerApp.class, args);
//		String s2 = "\"Quick-FDS [17484-30716-14059-014697] - 2015-11-13 - 08:31:56 \"";
//		String s1 = "Quick-FDS [17484-30716-14059-014697] - 2015-11-13 - 08:31:56";
//		System.out.println("size1=" + s1.length() +", size2=" + s2.length());
//		System.out.println(new LevenshteinDistance().apply(s1, s2));
//		System.out.println("threshold=" + s1.length()*0.3);
	}

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
//			tsvReaderService.processFile("sds6979071286215649937.tsv"); //OK
//			tsvReaderService.processFile("sds6470802517340212903.tsv"); //OK
//			tsvReaderService.processFile("sds5493674484985787039.tsv");
//			tsvReaderService.processFile("sds6548799556800349124.tsv"); //OK
			tsvReaderService.processFile("sds2912057926858607250.tsv"); //OK
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
