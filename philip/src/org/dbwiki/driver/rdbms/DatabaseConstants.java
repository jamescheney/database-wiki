/* 
    BEGIN LICENSE BLOCK
    Copyright 2010-2011, Heiko Mueller, Sam Lindley, James Cheney and
    University of Edinburgh

    This file is part of Database Wiki.

    Database Wiki is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Database Wiki is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Database Wiki.  If not, see <http://www.gnu.org/licenses/>.
    END LICENSE BLOCK
*/
package org.dbwiki.driver.rdbms;

/** Constants and strings used in the database classes */
public interface DatabaseConstants {
	/*
	 * Public Constants
	 */
	
	//
	// Annotation
	//
	public static final String RelationAnnotation = "_annotation";
	
	public static final String RelAnnotationColID     = "id";
	public static final String RelAnnotationColNode   = "node";
	public static final String RelAnnotationColParent = "parent";
	public static final String RelAnnotationColDate   = "date";
	public static final String RelAnnotationColUser   = "uid";
	public static final String RelAnnotationColText   = "text";
	
	//
	// Data
	//
	public static final String RelationData = "_data";
	
	public static final String RelDataColID           = "id";
	public static final String RelDataColParent       = "parent";
	public static final String RelDataColSchema       = "d_schema";
	public static final String RelDataColEntry        = "entry";
	public static final String RelDataColValue        = "value";
	public static final String RelDataColTimesequence = "timesequence";
	public static final String RelDataColPre = "pre";
	public static final String RelDataColPost = "post";
    

	public static final int RelDataColIDValUnknown        = -1;
	public static final int RelDataColSchemaValUnknown    = -1;
	public static final int RelDataColParentValUnknown    = -1;
	public static final int RelDataColTimestampValUnknown = -1;
	
	
	// Data view
	public static final String ViewData = "_vdata";
	
	public static final String ViewDataColNodeID         = "n_id";
	public static final String ViewDataColNodeParent     = "n_parent";
	public static final String ViewDataColNodeSchema     = "n_schema";
	public static final String ViewDataColNodeEntry      = "n_entry";
	public static final String ViewDataColNodeValue      = "n_value";
	public static final String ViewDataColNodePre      = "n_pre";
	public static final String ViewDataColNodePost      = "n_post";
	
	public static final String ViewDataColAnnotationID   = "a_id";
	public static final String ViewDataColAnnotationDate = "a_date";
	public static final String ViewDataColAnnotationUser = "a_uid";
	public static final String ViewDataColAnnotationText = "a_text";
	
	public static final String ViewDataColTimestampStart = "t_start";
	public static final String ViewDataColTimestampEnd   = "t_stop";
	
	// Schema index view
	public static final String ViewSchemaIndex = "_veindex";
	public static final String ViewSchemaIndexColMaxCount = "max_count";
	
	//
	// Pages
	//
	public static final String RelationPages = "_pages";
	
	public static final String RelPagesColID        = "id";
	public static final String RelPagesColName      = "name";
	public static final String RelPagesColContent   = "content";
	public static final String RelPagesColTimestamp = "timestamp";
	public static final String RelPagesColUser      = "uid";
	
	//
	// Schema
	//
	public static final String RelationSchema = "_schema";
	
	public static final String RelSchemaColID           = "id";
	public static final String RelSchemaColType         = "type";
	public static final String RelSchemaColLabel        = "name";
	public static final String RelSchemaColParent       = "parent";
	public static final String RelSchemaColUser         = "uid";
	public static final String RelSchemaColTimesequence = "timesequence";
	
	public static final int RelSchemaColParentValUnknown = -1;

	public static final int RelSchemaColTypeValAttribute = 0;
	public static final int RelSchemaColTypeValGroup     = 1;
	
	
	//
	// Timesequence
	//
	public static final String RelationTimesequence = "_timesequence";
	
	public static final String RelTimesequenceColID    = "id";
	public static final String RelTimesequenceColStart = "start";
	public static final String RelTimesequenceColStop  = "stop";

	public static final int RelTimestampColEndValOpen = -1;
	
	
	//
	// Version
	//
	public static final String RelationVersion = "_version";
	
	public static final String RelVersionColNumber      = "id";
	public static final String RelVersionColName        = "name";
	public static final String RelVersionColProvenance  = "provenance";
	public static final String RelVersionColUser        = "uid";
	public static final String RelVersionColNode        = "node";
	public static final String RelVersionColSource      = "source";
	public static final String RelVersionColTime        = "time";
	
	public static final int RelVersionColNodeValImport = -1;
}
