package org.innovateuk.ifs.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.config.IfsThymeleafExpressionObjectFactory;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;

import static java.util.Optional.ofNullable;
import static org.innovateuk.ifs.util.StringFunctions.countWords;

/**
 * This class provides utility methods that can be used in replacement for lengthy OGNL or SpringEL expressions in Thymeleaf.
 * <p>
 * These methods are offered by the #ifsUtil utility object in Thymeleaf variable expressions.
 * <p>
 * #ifsUtil is added to the evaluation context by {@link IfsThymeleafExpressionObjectFactory}.
 */
public class ThymeleafUtil {
    private static final Log LOG = LogFactory.getLog(ThymeleafUtil.class);
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mma");

    /**
     * Gets the uri for used for form posts.
     *
     * @param request
     * @return
     */
    public String formPostUri(final HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Cannot determine request URI with query string for null request.");
        }
        LOG.debug("Creating URI for request " + request.getClass() + " with URI " + request.getRequestURI());
        return UriComponentsBuilder.fromPath(request.getServletPath()).build().normalize().toUriString();
    }

    /**
     * <p>
     * Given a maximum word count allowed for an item of content, count the number of words remaining until this limit is reached, for the specified content {@link String}.
     * If the content contains HTML markup then this will first be parsed into a HTML Document and the text content will be extracted.
     * </p>
     *
     * @param maxWordCount
     * @param content
     * @return
     */
    public int wordsRemaining(Integer maxWordCount, String content) {
        return ofNullable(maxWordCount).map(maxWordCountValue -> maxWordCountValue - countWords(content)).orElse(0);
    }

    public long calculatePercentage(long part, long total){
        return Math.round(part * 100.0/total);
    }
}
