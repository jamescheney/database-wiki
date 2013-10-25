/*
 * Copyright (C) 2010 Mathias Doenitz
 *
 * Based on peg-markdown (C) 2008-2010 John MacFarlane
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

package org.pegdown;

public interface Extensions {
    /**
     * The default, standard markup mode without any extensions.
     */
    static final int NONE = 0x00;

    /**
     * Pretty ellipsises, dashes and apostrophes.
     */
    static final int SMARTS = 0x01;

    /**
     * Pretty single and double quotes.
     */
    static final int QUOTES = 0x02;

    /**
     * All of the smartypants prettyfications. Equivalent to SMARTS || QUOTES.
     * 
     * @see <a href="http://daringfireball.net/projects/smartypants/">Smartypants</a>
     */
    static final int SMARTYPANTS = 0x03;

    /**
     * PHP Markdown Extra style abbreviations.
     *
     * @see <a href="http://michelf.com/projects/php-markdown/extra/#abbr">PHP Markdown Extra</a>
     */
    static final int ABBREVIATIONS = 0x04;

    /**
     * Enables the parsing of hard wraps as HTML linebreaks. Similar to what github does.
     *
     * @see <a href="http://github.github.com/github-flavored-markdown">Github-flavored-Markdown</a>
     */
    static final int HARDWRAPS = 0x08;

    /**
     * Enables plain autolinks the way github flavoures markdown implements them.
     * With this extension enabled pegdown will intelligently recognize URLs and email adresses
     * without any further delimiters and mark them as the respective link type.
     *
     * @see <a href="http://github.github.com/github-flavored-markdown">Github-flavored-Markdown</a>
     */
    static final int AUTOLINKS = 0x10;

    /**
     * Enables table support similar to what Multimarkdown offers.
     *
     * @see <a href="http://fletcherpenney.net/multimarkdown/users_guide/multimarkdown_syntax_guide/">MultiMarkdown</a>
     */
    static final int TABLES = 0x20;

    /**
     * All available extensions not including the HTML SUPPRESSION options.
     */
    static final int ALL = 0x0000FFFF;

    /**
     * Suppresses HTML blocks. They will be accepted in the input but not be contained in the output.
     */
    static final int SUPPRESS_HTML_BLOCKS = 0x00010000;

    /**
     * Suppresses inline HTML tags. They will be accepted in the input but not be contained in the output.
     */
    static final int SUPPRESS_INLINE_HTML = 0x00020000;

    /**
     * Suppresses HTML blocks as well as inline HTML tags. Both will be accepted in the input but not be contained in the output.
     */
    static final int SUPPRESS_ALL_HTML = 0x00030000;

}
