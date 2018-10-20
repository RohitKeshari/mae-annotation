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

package edu.brandeis.llc.mae.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import edu.brandeis.llc.mae.MaeStrings;
import edu.brandeis.llc.mae.database.LinkTagDao;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by krim on 11/19/15.
 */

@DatabaseTable(tableName = DBSchema.TAB_LTAG, daoClass = LinkTagDao.class)
public class LinkTag extends Tag implements ModelI {

    @ForeignCollectionField(eager = true)
    private ForeignCollection<Argument> arguments;

    public LinkTag() {

    }

    public LinkTag(String tid, TagType tagType, String filename) {
        super(tid, tagType, filename);
    }

    public ForeignCollection<Argument> getArguments() {
        return arguments;
    }

    public ArrayList<ExtentTag> getArgumentTags() {
        ArrayList<ExtentTag> tags = new ArrayList<>();
        for (Argument arg : getArguments()) {
            tags.add(arg.getArgument());
        }
        return tags;
    }

    public Argument getArgumentByTypeName(String argTypeName) {
        for (Argument arg : getArguments()) {
            if (arg.getArgumentType().getName().equals(argTypeName)) {
                return arg;
            }
        }
        return null;
    }


    @Override
    public Set<String> getUnderspec() {
        Set<String> underpec = checkRequiredAtts();
        underpec.addAll(checkRequiredArgs());
        return underpec;
    }

    private Set<String> checkRequiredArgs() {
        Set<String> underspec = new TreeSet<>();
        ArrayList<ArgumentType> existingArgs = new ArrayList<>();
        for (Argument arg : getArguments()) {
            if (arg.isComplete()) {
                existingArgs.add(arg.getArgumentType());
            }
        }
        for (ArgumentType argType : getTagtype().getArgumentTypes()) {
            if (argType.isRequired() && !existingArgs.contains(argType)) {
                underspec.add(argType.getName());
            }
        }
        return underspec;

    }

    public Map<String, String> getArgumentTidsAndTextsWithNames() {
        Map<String, String> argumentsNames = new LinkedHashMap<>();
        if (getArguments() != null && getArguments().size() > 0) {
            for (Argument argument : getArguments()) {
                argumentsNames.put(argument.getName() + MaeStrings.ARG_IDCOL_SUF, argument.getArgument().getTid());
                argumentsNames.put(argument.getName() + MaeStrings.ARG_TEXTCOL_SUF, argument.getArgument().getText());
            }
        }
        return argumentsNames;

    }

    public Map<String, String> getArgumentTidsWithNames() {
        Map<String, String> argumentsWithNames = new LinkedHashMap<>();
        if (getArguments() != null && getArguments().size() > 0) {
            for (Argument argument : getArguments()) {
                argumentsWithNames.put(argument.getName(), argument.getArgument().getTid());
            }
        }
        return argumentsWithNames;

    }

    @Override
    public Map<String, String> getAttributesWithNames() {
        // make sure arguments are inserted before attributes
        Map<String, String> map = getArgumentTidsAndTextsWithNames();
        map.putAll(super.getAttributesWithNames());
        return map;
    }

    @Override
    public String toJsonString() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public String toXmlString() {
        // TODO 151229 maybe re-implement this (and one in ExtentTag) using DOM in the future for robustness?
        String tagTypeName = getTagTypeName();
        String idAtt = String.format("id=\"%s\"", tid);
        String attributes = getAttributesXmlString();
        return String.format("<%s />", StringUtils.join(new String[]{tagTypeName, idAtt, attributes}, " "));
    }

    @Override
    public String toString() {
        return String.format("%s-%s", getId(), getArgumentTidsWithNames().toString());
    }


}
