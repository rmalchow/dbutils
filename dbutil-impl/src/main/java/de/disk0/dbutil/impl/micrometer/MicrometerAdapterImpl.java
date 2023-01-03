package de.disk0.dbutil.impl.micrometer;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(name = "dbutil.micrometer.enabled", havingValue = "true")
public class MicrometerAdapterImpl implements MicrometerAdapter {

	private final ObjectProvider<MeterRegistry> meterRegistryProvider;

	@Override
	public void record(Duration duration, String metricName, Tag... tags) {
		MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable();
		if (meterRegistry == null) return;

		Iterable<io.micrometer.core.instrument.Tag> micrometerTags = Arrays.stream(tags)
				.map(tag -> new ImmutableTag(tag.getKey(), tag.getValue()))
				.collect(Collectors.toList());

		meterRegistry.timer(metricName, micrometerTags).record(duration);
	}

}
