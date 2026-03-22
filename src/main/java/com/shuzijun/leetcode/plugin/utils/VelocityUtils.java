package com.shuzijun.leetcode.plugin.utils;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.StringWriter;
import java.util.Properties;

/**
 * @author shuzijun
 */
public class VelocityUtils {

    private static String VM_LOG_TAG = "Leetcode VelocityUtils";
    private static String VM_CONTEXT = "question";
    private static final String[] DIRECTIVES_WITH_ARGUMENTS = {"if", "set", "foreach", "parse", "include", "macro", "define", "evaluate", "elseif"};
    private static final String[] DIRECTIVES_WITHOUT_ARGUMENTS = {"else", "end", "break", "stop"};
    private static VelocityEngine engine;


    static {
        engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.PARSER_POOL_SIZE, 20);
        engine.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
        //engine.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8");

        Properties props = new Properties();
        props.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        props.put("runtime.log.logsystem.log4j.category", "velocity");
        props.put("runtime.log.logsystem.log4j.logger", "velocity");

        engine.init(props);
    }

    public static String convert(String template, Object data) {

        StringWriter writer = new StringWriter();
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(VM_CONTEXT, data);
        velocityContext.put("velocityTool", new VelocityTool());
        velocityContext.put("vt", new VelocityTool());
        boolean isSuccess = engine.evaluate(velocityContext, writer, VM_LOG_TAG, escapeLiteralHashes(template));
        if (!isSuccess) {

        }
        return writer.toString();
    }

    private static String escapeLiteralHashes(String template) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        StringBuilder escaped = new StringBuilder(template.length());
        for (int i = 0; i < template.length(); i++) {
            char current = template.charAt(i);
            if (current == '#' && !isEscaped(template, i) && !isVelocityDirective(template, i)) {
                escaped.append('\\');
            }
            escaped.append(current);
        }
        return escaped.toString();
    }

    private static boolean isEscaped(String template, int index) {
        int slashCount = 0;
        for (int i = index - 1; i >= 0 && template.charAt(i) == '\\'; i--) {
            slashCount++;
        }
        return slashCount % 2 == 1;
    }

    private static boolean isVelocityDirective(String template, int index) {
        if (template.startsWith("##", index) || template.startsWith("#*", index)) {
            return true;
        }

        int directiveStart = index + 1;
        for (String directive : DIRECTIVES_WITH_ARGUMENTS) {
            if (template.startsWith(directive, directiveStart)) {
                int position = directiveStart + directive.length();
                while (position < template.length() && Character.isWhitespace(template.charAt(position))) {
                    position++;
                }
                if (position < template.length() && template.charAt(position) == '(') {
                    return true;
                }
            }
        }

        for (String directive : DIRECTIVES_WITHOUT_ARGUMENTS) {
            if (template.startsWith(directive, directiveStart)) {
                int position = directiveStart + directive.length();
                if (position >= template.length()) {
                    return true;
                }
                char next = template.charAt(position);
                if (!Character.isLetterOrDigit(next) && next != '_') {
                    return true;
                }
            }
        }

        return false;
    }
}
