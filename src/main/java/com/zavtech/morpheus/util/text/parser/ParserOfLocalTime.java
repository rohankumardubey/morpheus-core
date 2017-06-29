/**
 * Copyright (C) 2014-2017 Xavier Witdouck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zavtech.morpheus.util.text.parser;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zavtech.morpheus.util.functions.FunctionStyle;
import com.zavtech.morpheus.util.functions.ToBooleanFunction;
import com.zavtech.morpheus.util.text.FormatException;

/**
 * A Parser implementation for LocalTime objects.
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
class ParserOfLocalTime extends Parser<LocalTime> {

    private static final Map<Pattern,DateTimeFormatter> patternMap = new LinkedHashMap<>();

    private Supplier<DateTimeFormatter> format;

    /**
     * Static initializer
     */
    static {
        patternMap.put(Pattern.compile("\\d{2}:\\d{2}"), DateTimeFormatter.ofPattern("HH:mm"));
        patternMap.put(Pattern.compile("\\d{2}:\\d{2}:\\d{2}"), DateTimeFormatter.ofPattern("HH:mm:ss"));
        patternMap.put(Pattern.compile("\\d{2}:\\d{2}:\\d{2}\\.\\d+"), DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }

    /**
     * Constructor
     * @param nullChecker   the null checker function
     * @param format        the date format supplier, which may return a null format in order to use pattern matching
     */
    ParserOfLocalTime(ToBooleanFunction<String> nullChecker, Supplier<DateTimeFormatter> format) {
        super(FunctionStyle.OBJECT, LocalTime.class, nullChecker);
        this.format = format;
    }

    @Override
    public final boolean isSupported(String value) {
        if (!getNullChecker().applyAsBoolean(value)) {
            for (Map.Entry<Pattern, DateTimeFormatter> entry : patternMap.entrySet()) {
                final Matcher matcher = entry.getKey().matcher(value);
                if (matcher.reset(value).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public final LocalTime apply(String value) {
        try {
            if (getNullChecker().applyAsBoolean(value)) {
                return null;
            } else {
                final DateTimeFormatter formatter = format.get();
                if (formatter != null) {
                    return LocalTime.parse(value, formatter);
                } else {
                    for (Map.Entry<Pattern,DateTimeFormatter> entry : patternMap.entrySet()) {
                        final Matcher matcher = entry.getKey().matcher(value);
                        if (matcher.reset(value).matches()) {
                            return LocalTime.parse(value, entry.getValue());
                        }
                    }
                    throw new IllegalArgumentException("Unable to parse value into LocalDate: " + value);
                }
            }
        } catch (Exception ex) {
            throw new FormatException("Failed to parse value into LocalTime: " + value, ex);
        }
    }
}