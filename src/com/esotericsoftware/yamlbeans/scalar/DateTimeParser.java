/*
 * Copyright (c) 2006, Steve Leach, All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met: * Redistributions of source code must retain the above copyright notice, this list of conditions and the
 * following disclaimer. * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.esotericsoftware.yamlbeans.scalar;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/** A flexible date/time parser.
 * <p>
 * Attempts to interpret whatever text it is given as a date and/or time.
 * <p>
 * Implemented as a DateFormat subclass, so can also be used to format dates as strings.
 * 
 * @author Steve Leach */
class DateTimeParser extends DateFormat {
	private static final String DATEFORMAT_YAML = "yyyy-MM-dd HH:mm:ss";
	private static final int FORMAT_NONE = -1;

	private SimpleDateFormat outputFormat;
	private ArrayList<Parser> parsers = new ArrayList<Parser>();

	public DateTimeParser () {
		outputFormat = new SimpleDateFormat(DATEFORMAT_YAML);

		// First, try the format defined when creating the parser
		parsers.add(new SimpleParser(outputFormat));

		// Try the date as a single number, which is the milliseconds since January 1, 1970, 00:00:00 GMT
		parsers.add(new Parser() {
			public Date parse (String s) throws ParseException {
				try {
					long val = Long.parseLong(s);
					return new Date(val);
				} catch (NumberFormatException e) {
					throw new ParseException("Error parsing value", -1);
				}
			}
		});

		parsers.add(new SimpleParser("yyyy-MM-dd"));

		// Locale date & time
		parsers.add(new SimpleParser(DateFormat.FULL, DateFormat.FULL));
		parsers.add(new SimpleParser(DateFormat.LONG, DateFormat.LONG));
		parsers.add(new SimpleParser(DateFormat.MEDIUM, DateFormat.MEDIUM));
		parsers.add(new SimpleParser(DateFormat.SHORT, DateFormat.SHORT));

		// Date only
		parsers.add(new SimpleParser(DateFormat.FULL, FORMAT_NONE));
		parsers.add(new SimpleParser(DateFormat.LONG, FORMAT_NONE));
		parsers.add(new SimpleParser(DateFormat.MEDIUM, FORMAT_NONE));
		parsers.add(new SimpleParser(DateFormat.SHORT, FORMAT_NONE));

		// Time only
		parsers.add(new SimpleParser(FORMAT_NONE, DateFormat.FULL));
		parsers.add(new SimpleParser(FORMAT_NONE, DateFormat.LONG));
		parsers.add(new SimpleParser(FORMAT_NONE, DateFormat.MEDIUM));
		parsers.add(new SimpleParser(FORMAT_NONE, DateFormat.SHORT));
	}

	public Date parse (String text, ParsePosition pos) {
		String s = text.substring(pos.getIndex());
		Date date = null;

		for (Parser parser : parsers) {
			try {
				date = parser.parse(s);
				break;
			} catch (ParseException e) {
				// Ignore parse exceptions.
				// We are going to be trying lots of options, so many of
				// them are going to fail.
			}
		}

		if (date == null) {
			pos.setIndex(pos.getIndex());
			pos.setErrorIndex(pos.getIndex());
		} else {
			pos.setIndex(s.length());
		}

		return date;
	}

	public StringBuffer format (Date date, StringBuffer buf, FieldPosition pos) {
		return outputFormat.format(date, buf, pos);
	}

	/** Interface for parsers */
	static protected interface Parser {
		public Date parse (String s) throws ParseException;
	}

	/** Basic, flexible, parser implementation
	 * <p>
	 * A wrapper around DateFormat and SimpleDateFormat classes. */
	static protected class SimpleParser implements Parser {
		private DateFormat format;

		public SimpleParser (String format) {
			this.format = new SimpleDateFormat(format);
		}

		public SimpleParser (DateFormat format) {
			this.format = format;
		}

		public SimpleParser (int dateType, int timeType) {
			if (timeType < 0) {
				this.format = DateFormat.getDateInstance(dateType);
			} else if (dateType < 0) {
				this.format = DateFormat.getTimeInstance(timeType);
			} else {
				this.format = DateFormat.getDateTimeInstance(dateType, timeType);
			}
		}

		public Date parse (String s) throws ParseException {
			return format.parse(s);
		}
	}
}
