package code.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class VersionUtil {

    private static String[] V = new String[] {
            "v", "e", "r", "s", "i", "o", "n", "s"
    };

    public enum VersionCompareResult {
        LT(),
        EQ(),
        GT(),
        NULL(),
        ;

        VersionCompareResult () {}
    }

    private static String versionHandle(String version) {
        for (String s : V) {
            version = StringUtils.replace(version, s, "");
        }
        return version;
    }
    private static List<Integer> splitVersion(String version) {
        return Arrays.stream(StringUtils.split(version, ".")).map(Integer::valueOf).collect(Collectors.toList());
    }

    public static VersionCompareResult compare(String version1, String version2) {
        try {
            if (StringUtils.isBlank(version1) || StringUtils.isBlank(version2)) {
                return VersionCompareResult.NULL;
            } else if (version1.equals(version2)) {
                return VersionCompareResult.EQ;
            }
            String v1 = versionHandle(version1);
            String v2 = versionHandle(version2);
            if (v1.equals(v2)) {
                return VersionCompareResult.EQ;
            }
            List<Integer> v1Array = splitVersion(v1);
            List<Integer> v2Array = splitVersion(v2);

            if (v1Array.size() > v2Array.size()) {
                for (int i = v2Array.size(); i < v1Array.size(); i++) {
                    v2Array.add(0);
                }
            } else if (v2Array.size() > v1Array.size()) {
                for (int i = v1Array.size(); i < v2Array.size(); i++) {
                    v1Array.add(0);
                }
            }

            for (int i = 0 ; i < v1Array.size(); i++) {
                int i1 = v1Array.get(i).intValue();
                int i2 = v2Array.get(i).intValue();

                if (i1 > i2) {
                    return VersionCompareResult.GT;
                } else if (i1 < i2) {
                    return VersionCompareResult.LT;
                }
            }

            return VersionCompareResult.EQ;
        } catch (Exception e) {
            log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
        }
        return VersionCompareResult.NULL;
    }

}
