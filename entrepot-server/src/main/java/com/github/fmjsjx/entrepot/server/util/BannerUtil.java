package com.github.fmjsjx.entrepot.server.util;

import java.io.InputStream;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.fmjsjx.libcommon.util.ansi.AnsiColor;
import com.github.fmjsjx.libcommon.util.ansi.AnsiColorString;

public class BannerUtil {

    public static final void printBanner(Consumer<String> printAction, String projectName, String projectVersion) {
        printBanner(printAction, bannerInputStream(), projectName, projectVersion);
    }

    public static final void printGameBanner(Consumer<String> printAction, String projectName, String projectVersion) {
        printBanner(printAction, bannerInputStream(BannerUtil::douziGameInputStream), projectName, projectVersion);
    }

    static final InputStream bannerInputStream() {
        return bannerInputStream(BannerUtil::douziBannerInputStream);
    }

    static final InputStream bannerInputStream(Supplier<? extends InputStream> supplier) {
        return projectBannerInputStream().orElseGet(supplier);
    }

    static final Optional<InputStream> projectBannerInputStream() {
        return Optional.ofNullable(resourceStream("/project-banner"));
    }

    static final InputStream resourceStream(String name) {
        return BannerUtil.class.getResourceAsStream(name);
    }

    static final InputStream douziBannerInputStream() {
        return resourceStream("/douzi-banner");
    }

    static final InputStream douziGameInputStream() {
        return resourceStream("/douzi-game");
    }

    public static final void printBanner(Consumer<String> printAction, InputStream bannerInput, String projectName,
            String projectVersion) {
        var banner = new StringBuilder().append("%n");
        var maxWidth = 0;
        try (Scanner scanner = new Scanner(bannerInput)) {
            for (; scanner.hasNext();) {
                var line = scanner.nextLine().stripTrailing();
                banner.append(line).append("%n");
                maxWidth = Math.max(maxWidth, line.length());
            }
        }
        var projectNamePart = new AnsiColorString(AnsiColor.GREEN, " :: " + projectName + " :: ");
        var projectVersionPart = new AnsiColorString(AnsiColor.BRIGHT_WHITE, "(" + projectVersion + ")");
        int width = maxWidth - 10;
        var paddingSize = width - projectName.length() - projectVersion.length();
        var padding = paddingSize > 0 ? " ".repeat(paddingSize) : "";
        var line = new StringBuilder();
        projectNamePart.appendTo(line);
        line.append(padding);
        projectVersionPart.appendTo(line);
        banner.append(line.toString()).append("%n%n");
        printAction.accept(String.format(banner.toString()));
    }

    private BannerUtil() {
    }
}
