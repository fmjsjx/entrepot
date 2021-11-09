package com.github.fmjsjx.entrepot.server.cook;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.core.util.UuidUtil;

import com.github.fmjsjx.libcommon.json.JsoniterLibrary;
import com.github.fmjsjx.libcommon.util.ChecksumUtil;
import com.github.fmjsjx.libcommon.util.DigestUtil;
import com.jsoniter.any.Any;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MergeJsonFieldsCook implements Cook {

    private static final DateTimeFormatter ISO_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<Consumer<MergeContext>> fieldMergers;

    public MergeJsonFieldsCook(Map<String, String> fields) {
        var fieldMergers = this.fieldMergers = new ArrayList<>(fields.size());
        fields.forEach((field, value) -> {
            switch (value) {
            case "$address" -> fieldMergers.add(ctx -> ctx.put(field, Any.wrap(ctx.ip)));
            case "$timestamp" -> fieldMergers.add(ctx -> ctx.put(field, Any.wrap(ctx.getTimestamp())));
            case "$unix-time" -> fieldMergers.add(ctx -> ctx.put(field, Any.wrap(ctx.getUnixTime())));
            case "$iso-date" -> fieldMergers.add(ctx -> ctx.put(field, Any.wrap(ctx.getDatetime().toLocalDate())));
            case "$iso-time" -> fieldMergers
                    .add(ctx -> ctx.put(field, Any.wrap(ctx.getDatetime().toLocalTime().format(ISO_TIME))));
            case "$iso-datetime" -> fieldMergers
                    .add(ctx -> ctx.put(field, Any.wrap(ctx.getDatetime().format(ISO_DATETIME))));
            case "$datetime" -> fieldMergers.add(ctx -> ctx.put(field, Any.wrap(ctx.getDatetime().format(DATETIME))));
            case "$uuid", "$uuid-v1" -> fieldMergers
                    .add(ctx -> ctx.put(field, Any.wrap(UuidUtil.getTimeBasedUuid().toString())));
            case "$uuid-v4" -> fieldMergers.add(ctx -> ctx.put(field, Any.wrap(UUID.randomUUID().toString())));
            case "$md5" -> fieldMergers.add(ctx -> ctx.put(field, Any.wrap(DigestUtil.md5AsHex(ctx.raw))));
            case "$sha1" -> fieldMergers.add(ctx -> ctx.put(field, Any.wrap(DigestUtil.sha1AsHex(ctx.raw))));
            case "$sha256" -> fieldMergers.add(ctx -> ctx.put(field, Any.wrap(DigestUtil.sha256AsHex(ctx.raw))));
            case "$crc32" -> fieldMergers.add(ctx -> ctx.put(field, Any.wrap(ChecksumUtil.crc32(ctx.raw))));
            case "$crc32c" -> fieldMergers.add(ctx -> ctx.put(field, Any.wrap(ChecksumUtil.crc32c(ctx.raw))));
            default -> {
                // do nothing
            }
            }
        });
    }

    @Override
    public byte[] cook(byte[] raw, String remoteIp) {
        if (log.isDebugEnabled()) {
            log.debug("Cook raw data: {}", new String(raw, StandardCharsets.UTF_8));
        }
        var ctx = new MergeContext(raw, remoteIp);
        for (var merger : fieldMergers) {
            merger.accept(ctx);
        }
        var data = JsoniterLibrary.getInstance().dumpsToBytes(ctx.data);
        if (log.isDebugEnabled()) {
            log.debug("Cooked data: {}", new String(data, StandardCharsets.UTF_8));
        }
        return data;
    }

    private static final class MergeContext {

        private final byte[] raw;
        private final Map<String, Any> data;
        private final String ip;
        private long timestamp;
        private long unixTime;
        private LocalDateTime datetime;

        private MergeContext(byte[] raw, String ip) {
            this.raw = raw;
            this.data = JsoniterLibrary.getInstance().loads(raw).asMap();
            this.ip = ip;
        }

        private Map<String, Any> put(String field, Any value) {
            data.put(field, value);
            return data;
        }

        private long getUnixTime() {
            var unixTime = this.unixTime;
            if (unixTime == 0) {
                this.unixTime = unixTime = getTimestamp() / 1000;
            }
            return unixTime;
        }

        private long getTimestamp() {
            var timestamp = this.timestamp;
            if (timestamp == 0) {
                this.timestamp = timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }

        private LocalDateTime getDatetime() {
            var datetime = this.datetime;
            if (datetime == null) {
                this.datetime = datetime = LocalDateTime.ofInstant(Instant.ofEpochMilli(getTimestamp()),
                        ZoneId.systemDefault());
            }
            return datetime;
        }

    }

}
