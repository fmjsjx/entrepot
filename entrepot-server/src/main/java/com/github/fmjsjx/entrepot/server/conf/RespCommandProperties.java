package com.github.fmjsjx.entrepot.server.conf;

import java.util.Arrays;
import java.util.Map;
import java.util.function.IntFunction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.libnetty.resp.RespMessage;
import com.github.fmjsjx.libnetty.resp.RespMessages;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Properties class for RESP/RESP3 command.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class RespCommandProperties {

    /**
     * The command.
     */
    private Command command;

    /**
     * If batch mode is enabled or not.
     * <p>
     * The default value is {@code false}.
     */
    private boolean batchEnabled;
    /**
     * The type of the hangar.
     * <p>
     * The default value is {@code raw}.
     */
    private WharfType type;
    /**
     * The processor. Only effective when type is {@code cook}
     */
    private String processor;
    /**
     * The field definitions. Only effective when processor is
     * {@code merge-json-fields}.
     */
    private Map<String, String> fields;

    @JsonCreator
    public RespCommandProperties(@JsonProperty(value = "command", required = true) String command,
            @JsonProperty("batch-enabled") boolean batchEnabled, @JsonProperty("type") String type,
            @JsonProperty("processor") String processor, @JsonProperty("fields") Map<String, String> fields) {
        this.command = Command.of(command);
        this.batchEnabled = batchEnabled;
        this.type = WharfType.of(type);
        this.processor = processor;
        this.fields = fields;
    }

    public void validate() {
        // TODO
    }

    /**
     * Enumeration if supported commands.
     */
    public enum Command {

        APPEND(n -> RespMessages.one()),

        SET(n -> RespMessages.ok()),

        SETNX(n -> RespMessages.ok()),

        RPUSH(RespMessages::integer, Command.BATCH),

        RPUSHX(RespMessages::integer, Command.BATCH),

        LPUSH(RespMessages::integer, Command.BATCH),

        LPUSHX(RespMessages::integer, Command.BATCH),

        SADD(RespMessages::integer, Command.BATCH),

        ;

        public static final int BATCH = 0x1;

        public static final Command of(String command) {
            if (command == null) {
                throw new IllegalArgumentException("missing required parameter `command`");
            }
            return Arrays.stream(values()).filter(o -> command.equalsIgnoreCase(o.name())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("unsupported command `" + command + "`"));
        }

        private final IntFunction<RespMessage> resultMapper;
        private final int flag;

        private Command(IntFunction<RespMessage> resultMapper) {
            this(resultMapper, 0);
        }

        private Command(IntFunction<RespMessage> resultMapper, int flag) {
            this.resultMapper = resultMapper;
            this.flag = flag;
        }

        public IntFunction<RespMessage> resultMapper() {
            return resultMapper;
        }

        public RespMessage result(int number) {
            return resultMapper.apply(number);
        }

        public int flag() {
            return flag;
        }

        public boolean with(int flag) {
            return (this.flag & flag) != 0;
        }

        public boolean batchSupported() {
            return with(BATCH);
        }

    }

}
