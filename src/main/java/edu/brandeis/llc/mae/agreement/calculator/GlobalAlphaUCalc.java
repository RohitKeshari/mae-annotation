/*
 * MAE - Multi-purpose Annotation Environment
 *
 * Copyright Keigh Rim (krim@brandeis.edu)
 * Department of Computer Science, Brandeis University
 * Original program by Amber Stubbs (astubbs@cs.brandeis.edu)
 *
 * MAE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, @see <a href="http://www.gnu.org/licenses">http://www.gnu.org/licenses</a>.
 *
 * For feedback, reporting bugs, use the project on Github
 * @see <a href="https://github.com/keighrim/mae-annotation">https://github.com/keighrim/mae-annotation</a>.
 */

package edu.brandeis.llc.mae.agreement.calculator;

import edu.brandeis.llc.mae.agreement.io.AbstractAnnotationIndexer;
import edu.brandeis.llc.mae.agreement.io.XMLParseCache;
import edu.brandeis.llc.mae.database.MaeDBException;
import edu.brandeis.llc.mae.io.MaeXMLParser;
import edu.brandeis.llc.mae.util.MappedSet;
import org.dkpro.statistics.agreement.unitizing.KrippendorffAlphaUnitizingAgreement;
import org.dkpro.statistics.agreement.unitizing.UnitizingAnnotationStudy;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by krim on 4/23/2016.
 */
public class GlobalAlphaUCalc extends AbstractUnitizationAgreementCalc {


    public GlobalAlphaUCalc(AbstractAnnotationIndexer fileIdx, XMLParseCache parseCache, int[] documentLength) {
        super(fileIdx, parseCache, documentLength);
    }

    @Override
    public Map<String, Double> calculateAgreement(MappedSet<String, String> targetTagsAndAtts, boolean allowMultiTagging) throws IOException, SAXException, MaeDBException {
        Map<String, Double> globalAlphaU = new TreeMap<>();

        UnitizingAnnotationStudy study = new UnitizingAnnotationStudy(numAnnotators, totalDocumentsLength);
        int curDocLength = 0;
        List<String> documents = fileIdx.getDocumentNames();
        for (int i = 0; i < documents.size(); i++) {
            String document = documents.get(i);
            MaeXMLParser[] parses = parseCache.getParses(document);
            for (String tagTypeName : targetTagsAndAtts.keyList()) {
                addTagAsUnits(tagTypeName, parses, curDocLength, study);
            }
            curDocLength += documentLength[i];
        }
        double agree = (new KrippendorffAlphaUnitizingAgreement(study)).calculateAgreement();
        globalAlphaU.put("cross-tag_alpha_u", agree);
        return globalAlphaU;

    }

}
