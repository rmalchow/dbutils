package de.disk0.dbutil.impl.micrometer;

import lombok.Value;

import java.time.Duration;

public interface MicrometerAdapter {

	void record(Duration duration, String metricName, Tag... tags);

	@Value
	class Tag {
		String key;
		String value;

		public static Tag of(String key, String value) {
			return new Tag(key, value);
		}
	}

}
