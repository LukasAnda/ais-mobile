/*
 * Copyright 2020 Lukáš Anda. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.htmlcleaner;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>This class contains map with special entities used in HTML and their
 * unicodes.</p>
 * <p>
 * Created by: Vladimir Nikic<br/>
 * Date: November, 2006.
 */
public class SpecialEntities {

    public static final SpecialEntities INSTANCE = new SpecialEntities(true, true) {
        @Override
        public void put(SpecialEntity specialEntity) {
            throw new UnsupportedOperationException("cannot add to this instance");
        }
    };
    public static final char NON_BREAKABLE_SPACE = 160;
    /**
     * key is the {@link SpecialEntity#getKey()} ( i.e. "quot" )
     */
    private Map<String, SpecialEntity> entities = new HashMap<String, SpecialEntity>();
    /**
     * Key is the Integer returned by {@link SpecialEntity#intValue()}
     */
    private Map<Integer, SpecialEntity> entitiesByUnicodeCharcode = new HashMap<Integer, SpecialEntity>();
    private boolean greek;
    private boolean math;
    private int maxEntityLength;

    public SpecialEntities(boolean greek, boolean math) {
        this.greek = greek;
        this.math = math;
        _put(new SpecialEntity("null", 0, "", true));
        _put(new SpecialEntity("nbsp", NON_BREAKABLE_SPACE, null, true));
        _put(new SpecialEntity("iexcl", 161, null, true));
        _put(new SpecialEntity("cent", 162, null, true));
        _put(new SpecialEntity("pound", 163, null, true));
        _put(new SpecialEntity("curren", 164, null, true));
        _put(new SpecialEntity("yen", 165, null, true));
        _put(new SpecialEntity("brvbar", 166, null, true));
        _put(new SpecialEntity("sect", 167, null, true));
        _put(new SpecialEntity("uml", 168, null, true));
        _put(new SpecialEntity("copy", 169, null, true));
        _put(new SpecialEntity("ordf", 170, null, true));
        _put(new SpecialEntity("laquo", 171, null, true));
        _put(new SpecialEntity("not", 172, null, true));
        _put(new SpecialEntity("shy", 173, null, true));
        _put(new SpecialEntity("reg", 174, null, true));
        _put(new SpecialEntity("macr", 175, null, true));
        _put(new SpecialEntity("deg", 176, null, true));
        _put(new SpecialEntity("plusmn", 177, null, true));
        _put(new SpecialEntity("sup2", 178, null, true));
        _put(new SpecialEntity("sup3", 179, null, true));
        _put(new SpecialEntity("acute", 180, null, true));
        _put(new SpecialEntity("micro", 181, null, true));
        _put(new SpecialEntity("para", 182, null, true));
        _put(new SpecialEntity("middot", 183, null, true));
        _put(new SpecialEntity("cedil", 184, null, true));
        _put(new SpecialEntity("sup1", 185, null, true));
        _put(new SpecialEntity("ordm", 186, null, true));
        _put(new SpecialEntity("raquo", 187, null, true));
        _put(new SpecialEntity("frac14", 188, null, true));
        _put(new SpecialEntity("frac12", 189, null, true));
        _put(new SpecialEntity("frac34", 190, null, true));
        _put(new SpecialEntity("iquest", 191, null, true));
        _put(new SpecialEntity("Agrave", 192, null, true));
        _put(new SpecialEntity("Aacute", 193, null, true));
        _put(new SpecialEntity("Acirc", 194, null, true));
        _put(new SpecialEntity("Atilde", 195, null, true));

        _put(new SpecialEntity("Auml", 196, null, true));
        _put(new SpecialEntity("Aring", 197, null, true));
        _put(new SpecialEntity("AElig", 198, null, true));
        _put(new SpecialEntity("Ccedil", 199, null, true));
        _put(new SpecialEntity("Egrave", 200, null, true));
        _put(new SpecialEntity("Eacute", 201, null, true));
        _put(new SpecialEntity("Ecirc", 202, null, true));
        _put(new SpecialEntity("Euml", 203, null, true));
        _put(new SpecialEntity("Igrave", 204, null, true));
        _put(new SpecialEntity("Iacute", 205, null, true));
        _put(new SpecialEntity("Icirc", 206, null, true));
        _put(new SpecialEntity("Iuml", 207, null, true));
        _put(new SpecialEntity("ETH", 208, null, true));
        _put(new SpecialEntity("Ntilde", 209, null, true));
        _put(new SpecialEntity("Ograve", 210, null, true));
        _put(new SpecialEntity("Oacute", 211, null, true));
        _put(new SpecialEntity("Ocirc", 212, null, true));
        _put(new SpecialEntity("Otilde", 213, null, true));
        _put(new SpecialEntity("Ouml", 214, null, true));
        _put(new SpecialEntity("times", 215, null, true));
        _put(new SpecialEntity("Oslash", 216, null, true));
        _put(new SpecialEntity("Ugrave", 217, null, true));
        _put(new SpecialEntity("Uacute", 218, null, true));
        _put(new SpecialEntity("Ucirc", 219, null, true));
        _put(new SpecialEntity("Uuml", 220, null, true));
        _put(new SpecialEntity("Yacute", 221, null, true));
        _put(new SpecialEntity("THORN", 222, null, true));
        _put(new SpecialEntity("szlig", 223, null, true));
        _put(new SpecialEntity("agrave", 224, null, true));
        _put(new SpecialEntity("aacute", 225, null, true));
        _put(new SpecialEntity("acirc", 226, null, true));
        _put(new SpecialEntity("atilde", 227, null, true));
        _put(new SpecialEntity("auml", 228, null, true));
        _put(new SpecialEntity("aring", 229, null, true));
        _put(new SpecialEntity("aelig", 230, null, true));
        _put(new SpecialEntity("ccedil", 231, null, true));
        _put(new SpecialEntity("egrave", 232, null, true));
        _put(new SpecialEntity("eacute", 233, null, true));
        _put(new SpecialEntity("ecirc", 234, null, true));
        _put(new SpecialEntity("euml", 235, null, true));
        _put(new SpecialEntity("igrave", 236, null, true));
        _put(new SpecialEntity("iacute", 237, null, true));
        _put(new SpecialEntity("icirc", 238, null, true));
        _put(new SpecialEntity("iuml", 239, null, true));
        _put(new SpecialEntity("eth", 240, null, true));
        _put(new SpecialEntity("ntilde", 241, null, true));
        _put(new SpecialEntity("ograve", 242, null, true));
        _put(new SpecialEntity("oacute", 243, null, true));
        _put(new SpecialEntity("ocirc", 244, null, true));
        _put(new SpecialEntity("otilde", 245, null, true));
        _put(new SpecialEntity("ouml", 246, null, true));
        _put(new SpecialEntity("divide", 247, null, true));
        _put(new SpecialEntity("oslash", 248, null, true));
        _put(new SpecialEntity("ugrave", 249, null, true));
        _put(new SpecialEntity("uacute", 250, null, true));
        _put(new SpecialEntity("ucirc", 251, null, true));
        _put(new SpecialEntity("uuml", 252, null, true));
        _put(new SpecialEntity("yacute", 253, null, true));
        _put(new SpecialEntity("thorn", 254, null, true));
        _put(new SpecialEntity("yuml", 255, null, true));

        _put(new SpecialEntity("OElig", 338, null, true));
        _put(new SpecialEntity("oelig", 339, null, true));
        _put(new SpecialEntity("Scaron", 352, null, true));
        _put(new SpecialEntity("scaron", 353, null, true));
        _put(new SpecialEntity("Yuml", 376, null, true));
        _put(new SpecialEntity("fnof", 402, null, true));
        _put(new SpecialEntity("circ", 710, null, true));
        _put(new SpecialEntity("tilde", 732, null, true));
        if (this.greek) {
            // 913    Alpha      greek capital letter alpha
            _put(new SpecialEntity("Alpha", 913, null, true));
            // 914 Beta       greek capital letter beta
            _put(new SpecialEntity("Beta", 914, null, true));
            // 915 Gamma      greek capital letter gamma
            _put(new SpecialEntity("Gamma", 915, null, true));
            // 916 Delta      greek capital letter delta
            _put(new SpecialEntity("Delta", 916, null, true));
            // 917 Epsilon    greek capital letter epsilon
            _put(new SpecialEntity("Epsilon", 917, null, true));
            // 918 Zeta       greek capital letter zeta
            _put(new SpecialEntity("Zeta", 918, null, true));
            // 919 Eta    greek capital letter eta
            _put(new SpecialEntity("Eta", 919, null, true));
            // 920 Theta      greek capital letter theta
            _put(new SpecialEntity("Theta", 920, null, true));
            // 921 Iota       greek capital letter iota
            _put(new SpecialEntity("Iota", 921, null, true));
            // 922 Kappa      greek capital letter kappa
            _put(new SpecialEntity("Kappa", 922, null, true));
            // 923 Lambda     greek capital letter lambda
            _put(new SpecialEntity("Lambda", 923, null, true));
            // 924 Mu     greek capital letter mu
            _put(new SpecialEntity("Mu", 924, null, true));
            // 925 Nu     greek capital letter nu
            _put(new SpecialEntity("Nu", 925, null, true));
            // 926 Xi     greek capital letter xi
            _put(new SpecialEntity("Xi", 926, null, true));
            // 927 Omicron    greek capital letter omicron
            _put(new SpecialEntity("Omicron", 927, null, true));
            // 928 Pi   greek capital letter pi
            _put(new SpecialEntity("Pi", 928, null, true));
            // 929 Rho    greek capital letter rho
            _put(new SpecialEntity("Rho", 929, null, true));
            // there is no Sigmaf, and no U+03A2 character either
            // 931 Sigma      greek capital letter sigma
            _put(new SpecialEntity("Sigma", 931, null, true));
            // 932 Tau    greek capital letter tau
            _put(new SpecialEntity("Tau", 932, null, true));
            // 933 Upsilon    greek capital letter upsilon
            _put(new SpecialEntity("Upsilon", 933, null, true));
            // 934 Phi    greek capital letter phi
            _put(new SpecialEntity("Phi", 934, null, true));
            // 935 Chi    greek capital letter chi
            _put(new SpecialEntity("Chi", 935, null, true));
            // 936 Psi    greek capital letter psi
            _put(new SpecialEntity("Psi", 936, null, true));
            // 937 Omega      greek capital letter omega
            _put(new SpecialEntity("Omega", 937, null, true));
            // 945 alpha      greek small letter alpha
            _put(new SpecialEntity("alpha", 945, null, true));
            // 946 beta       greek small letter beta
            _put(new SpecialEntity("beta", 946, null, true));
            // 947 gamma      greek small letter gamma
            _put(new SpecialEntity("gamma", 947, null, true));
            // 948 delta      greek small letter delta
            _put(new SpecialEntity("delta", 948, null, true));
            // 949 epsilon    greek small letter epsilon
            _put(new SpecialEntity("epsilon", 949, null, true));
            // 950 zeta       greek small letter zeta
            _put(new SpecialEntity("zeta", 950, null, true));
            // 951 eta    greek small letter eta
            _put(new SpecialEntity("eta", 951, null, true));
            // 952 theta      greek small letter theta
            _put(new SpecialEntity("theta", 952, null, true));
            // 953 iota       greek small letter iota
            _put(new SpecialEntity("iota", 953, null, true));
            // 954 kappa      greek small letter kappa
            _put(new SpecialEntity("kappa", 954, null, true));
            // 955 lambda     greek small letter lambda
            _put(new SpecialEntity("lambda", 955, null, true));
            // 956 mu     greek small letter mu
            _put(new SpecialEntity("mu", 956, null, true));
            // 957 nu     greek small letter nu
            _put(new SpecialEntity("nu", 957, null, true));
            // 958 xi     greek small letter xi
            _put(new SpecialEntity("xi", 958, null, true));
            // 959 omicron    greek small letter omicron
            _put(new SpecialEntity("omicron", 959, null, true));
            // 960 pi     greek small letter pi
            _put(new SpecialEntity("pi", 960, null, true));
            // 961 rho    greek small letter rho
            _put(new SpecialEntity("rho", 961, null, true));
            // 962 sigmaf     greek small letter final sigma
            _put(new SpecialEntity("sigmaf", 962, null, true));
            // 963 sigma      greek small letter sigma
            _put(new SpecialEntity("sigma", 963, null, true));
            // 964 tau    greek small letter tau
            _put(new SpecialEntity("tau", 964, null, true));
            // 965 upsilon    greek small letter upsilon
            _put(new SpecialEntity("upsilon", 965, null, true));
            // 966 phi    greek small letter phi
            _put(new SpecialEntity("phi", 966, null, true));
            // 967 chi    greek small letter chi
            _put(new SpecialEntity("chi", 967, null, true));
            // 968 psi    greek small letter psi
            _put(new SpecialEntity("psi", 968, null, true));
            // 969 omega      greek small letter omega
            _put(new SpecialEntity("omega", 969, null, true));
            // 977 thetasym       greek small letter theta symbol
            _put(new SpecialEntity("thetasym", 977, null, true));
            // 978 upsih     greek upsilon with hook symbol
            _put(new SpecialEntity("upsih", 978, null, true));
            // 982 piv    greek pi symbol
            _put(new SpecialEntity("piv", 982, null, true));
        }
        _put(new SpecialEntity("ensp", 8194, null, true));
        _put(new SpecialEntity("emsp", 8195, null, true));
        _put(new SpecialEntity("thinsp", 8201, null, true));
        _put(new SpecialEntity("zwnj", 8204, null, true));
        _put(new SpecialEntity("zwj", 8205, null, true));
        _put(new SpecialEntity("lrm", 8206, null, true));
        _put(new SpecialEntity("rlm", 8207, null, true));
        _put(new SpecialEntity("ndash", 8211, null, true));
        _put(new SpecialEntity("mdash", 8212, null, true));
        _put(new SpecialEntity("lsquo", 8216, null, true));
        _put(new SpecialEntity("rsquo", 8217, null, true));
        _put(new SpecialEntity("sbquo", 8218, null, true));
        _put(new SpecialEntity("ldquo", 8220, null, true));
        _put(new SpecialEntity("rdquo", 8221, null, true));
        _put(new SpecialEntity("bdquo", 8222, null, true));
        _put(new SpecialEntity("dagger", 8224, null, true));
        _put(new SpecialEntity("Dagger", 8225, null, true));
        _put(new SpecialEntity("bull", 8226, null, true));
        // three ellipses
        _put(new SpecialEntity("hellip", 8230, null, true));
        _put(new SpecialEntity("permil", 8240, null, true));
        _put(new SpecialEntity("prime", 8242, null, true));
        _put(new SpecialEntity("Prime", 8243, null, true));
        _put(new SpecialEntity("lsaquo", 8249, null, true));
        _put(new SpecialEntity("rsaquo", 8250, null, true));
        _put(new SpecialEntity("oline", 8254, null, true));
        _put(new SpecialEntity("frasl", 8260, null, true));
        _put(new SpecialEntity("euro", 8364, null, true));
        _put(new SpecialEntity("image", 8465, null, true));
        _put(new SpecialEntity("weierp", 8472, null, true));
        _put(new SpecialEntity("real", 8476, null, true));
        _put(new SpecialEntity("trade", 8482, null, true));
        _put(new SpecialEntity("alefsym", 8501, null, true));
        _put(new SpecialEntity("larr", 8592, null, true));
        _put(new SpecialEntity("uarr", 8593, null, true));
        _put(new SpecialEntity("rarr", 8594, null, true));
        _put(new SpecialEntity("darr", 8595, null, true));
        _put(new SpecialEntity("harr", 8596, null, true));
        _put(new SpecialEntity("crarr", 8629, null, true));
        _put(new SpecialEntity("lArr", 8656, null, true));
        _put(new SpecialEntity("uArr", 8657, null, true));
        _put(new SpecialEntity("rArr", 8658, null, true));
        _put(new SpecialEntity("dArr", 8659, null, true));
        _put(new SpecialEntity("hArr", 8660, null, true));
        if (this.math) {
            _put(new SpecialEntity("forall", 8704, null, true));
            _put(new SpecialEntity("part", 8706, null, true));
            _put(new SpecialEntity("exist", 8707, null, true));
            _put(new SpecialEntity("empty", 8709, null, true));
            _put(new SpecialEntity("nabla", 8711, null, true));
            _put(new SpecialEntity("isin", 8712, null, true));
            _put(new SpecialEntity("notin", 8713, null, true));
            _put(new SpecialEntity("ni", 8715, null, true));
            _put(new SpecialEntity("prod", 8719, null, true));
            _put(new SpecialEntity("sum", 8721, null, true));
            _put(new SpecialEntity("minus", 8722, null, true));
            _put(new SpecialEntity("lowast", 8727, null, true));
            _put(new SpecialEntity("radic", 8730, null, true));
            _put(new SpecialEntity("prop", 8733, null, true));
            _put(new SpecialEntity("infin", 8734, null, true));
            _put(new SpecialEntity("ang", 8736, null, true));
            _put(new SpecialEntity("and", 8743, null, true));
            _put(new SpecialEntity("or", 8744, null, true));
            _put(new SpecialEntity("cap", 8745, null, true));
            _put(new SpecialEntity("cup", 8746, null, true));
            _put(new SpecialEntity("int", 8747, null, true));
            _put(new SpecialEntity("there4", 8756, null, true));
            _put(new SpecialEntity("sim", 8764, null, true));
            _put(new SpecialEntity("cong", 8773, null, true));
            _put(new SpecialEntity("asymp", 8776, null, true));
            _put(new SpecialEntity("ne", 8800, null, true));
            _put(new SpecialEntity("equiv", 8801, null, true));
            _put(new SpecialEntity("le", 8804, null, true));
            _put(new SpecialEntity("ge", 8805, null, true));
            _put(new SpecialEntity("sub", 8834, null, true));
            _put(new SpecialEntity("sup", 8835, null, true));
            _put(new SpecialEntity("nsub", 8836, null, true));
            _put(new SpecialEntity("sube", 8838, null, true));
            _put(new SpecialEntity("supe", 8839, null, true));
            _put(new SpecialEntity("oplus", 8853, null, true));
            _put(new SpecialEntity("otimes", 8855, null, true));
            _put(new SpecialEntity("perp", 8869, null, true));
            _put(new SpecialEntity("sdot", 8901, null, true));
            _put(new SpecialEntity("lceil", 8968, null, true));
            _put(new SpecialEntity("rceil", 8969, null, true));
            _put(new SpecialEntity("lfloor", 8970, null, true));
            _put(new SpecialEntity("rfloor", 8971, null, true));
            _put(new SpecialEntity("lang", 9001, null, true));
            _put(new SpecialEntity("rang", 9002, null, true));
            _put(new SpecialEntity("loz", 9674, null, true));
            _put(new SpecialEntity("spades", 9824, null, true));
            _put(new SpecialEntity("clubs", 9827, null, true));
            _put(new SpecialEntity("hearts", 9829, null, true));
            _put(new SpecialEntity("diams", 9830, null, true));
        }
        _put(new SpecialEntity("amp", '&', null, false));
        _put(new SpecialEntity("lt", '<', null, false));
        _put(new SpecialEntity("gt", '>', null, false));
        _put(new SpecialEntity("quot", '"', null, false));
        // this is xml only -- apos appearing in html needs to be converted to ' or maybe &#39; to be universally safe
        // may need to special case for html attributes that use ' as surrounding delimeter on attribute value (instead of " ) : <a href='javascript:foo("bar'")' >wierd link</a>
        _put(new SpecialEntity("apos", '\'', "'", false));
    }

    /**
     * @param seq may have a leading & and/or trailing ; ( those will be removed prior to comparision)
     * @return {@link SpecialEntity} if found.
     */
    public SpecialEntity getSpecialEntity(String seq) {
        if (seq.length() == 0) return null;
        int startIndex = seq.charAt(0) == '&' ? 1 : 0;
        int semiIndex = seq.indexOf(';');
        String entity;
        if (semiIndex < 0) {
            entity = seq.substring(startIndex);
        } else {
            entity = seq.substring(startIndex, semiIndex);
        }
        SpecialEntity specialEntity = entities.get(entity);
        return specialEntity;
    }

    public SpecialEntity getSpecialEntityByUnicode(int unicodeCharcode) {
        return this.entitiesByUnicodeCharcode.get(unicodeCharcode);
    }

    public void put(SpecialEntity specialEntity) {
        _put(specialEntity);
    }

    /**
     * @param specialEntity
     */
    private void _put(SpecialEntity specialEntity) {
        SpecialEntity old;
        old = entities.put(specialEntity.getKey(), specialEntity);
        if (old != null) {
            throw new HtmlCleanerException("replaced " + old + " with " + specialEntity);
        }
        old = entitiesByUnicodeCharcode.put(specialEntity.intValue(), specialEntity);
        if (old != null) {
            throw new HtmlCleanerException("replaced " + old + " with " + specialEntity);
        }
        this.maxEntityLength = Math.max(this.maxEntityLength, specialEntity.getKey().length());
    }

    public int getMaxEntityLength() {
        return maxEntityLength;
    }
}