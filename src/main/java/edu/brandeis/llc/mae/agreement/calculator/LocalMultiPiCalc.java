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

import edu.brandeis.llc.mae.MaeException;
import edu.brandeis.llc.mae.agreement.io.AbstractAnnotationIndexer;
import edu.brandeis.llc.mae.agreement.io.XMLParseCache;
import edu.brandeis.llc.mae.util.MappedSet;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.FleissKappaAgreement;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by krim on 4/25/2016.
 */
public class LocalMultiPiCalc extends AbstractCodingAgreementCalc {

    public LocalMultiPiCalc(AbstractAnnotationIndexer fileIdx, XMLParseCache parseCache) {
        super(fileIdx, parseCache);
    }

    @Override
    public Map<String, Double> calculateAgreement(MappedSet<String, String> targetTagsAndAtts, boolean allowMultiTagging) throws IOException, SAXException, MaeException {
        Map<String, Double> localMultiPi = new TreeMap<>();
        Map<String, CodingAnnotationStudy> studies = prepareLocalCodingStudies(targetTagsAndAtts, allowMultiTagging);
        for (String attFullName : studies.keySet()) {
            localMultiPi.put(attFullName, (new FleissKappaAgreement(studies.get(attFullName))).calculateAgreement());
        }
        return localMultiPi;
    }
}
