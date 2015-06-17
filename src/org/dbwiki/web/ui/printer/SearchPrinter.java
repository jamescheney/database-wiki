package org.dbwiki.web.ui.printer;

import org.dbwiki.data.resource.DatabaseIdentifier;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterList;
import org.dbwiki.web.ui.CSS;

public class SearchPrinter extends HtmlContentPrinter {
	private RequestParameterList _parameters;
	private DatabaseIdentifier _dbIdentifier;
	public SearchPrinter(RequestParameterList parameters, DatabaseIdentifier dbIdentifier ) {
		_parameters = parameters;
		_dbIdentifier = dbIdentifier;
	}
	@Override
	public void print(HtmlLinePrinter printer) throws WikiException {
		String searchParameters = "";
		if (_parameters.hasParameter(RequestParameter.ParameterSearch)) {
			RequestParameter searchParameter = _parameters.get(RequestParameter.ParameterSearch);
			if (searchParameter.hasValue()) {
				searchParameters = searchParameters + searchParameter.value();
			}
		}
		
		printer.openFORM("frmSearch", "GET", _dbIdentifier.databaseHomepage());
		printer.addTEXTBOXCSS("search", searchParameters, CSS.CSSSearch);
		printer.closeFORM();
	}	

}
