package com.douzi.carrier.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.github.fmjsjx.entrepot.core.Constants;

public class CarrierConstantsTest {

    @Test
    public void testDatetimePlaceholderPattern() {
        var str = "test-%d{yyyy-MM-dd'T'HH}.log";
        var pattern = Pattern.compile(Constants.DATETIME_PLACEHOLDER_REGEX);
        var matcher = pattern.matcher(str);
        if (matcher.find()) {
            var value = matcher.group();
            assertEquals("%d{yyyy-MM-dd'T'HH}", value);
            assertEquals(2, matcher.groupCount());
            assertEquals("yyyy-MM-dd'T'HH", matcher.group(2));
            assertEquals(5, matcher.start());
            assertEquals(24, matcher.end());
            assertEquals("test-", str.substring(0, matcher.start()));
            assertEquals(".log", str.substring(matcher.end()));
        }
        
        str = "test-%datetime{yyyy-MM-dd'T'HH}.log";
        pattern = Pattern.compile(Constants.DATETIME_PLACEHOLDER_REGEX);
        matcher = pattern.matcher(str);
        if (matcher.find()) {
            var value = matcher.group();
            assertEquals("%datetime{yyyy-MM-dd'T'HH}", value);
            assertEquals(2, matcher.groupCount());
            assertEquals("yyyy-MM-dd'T'HH", matcher.group(2));
            assertEquals(5, matcher.start());
            assertEquals(31, matcher.end());
            assertEquals("test-", str.substring(0, matcher.start()));
            assertEquals(".log", str.substring(matcher.end()));
        }
    }

}
