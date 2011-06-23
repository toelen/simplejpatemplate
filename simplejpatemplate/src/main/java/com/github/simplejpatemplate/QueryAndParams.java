package com.github.simplejpatemplate;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class QueryAndParams {
	private final String query;
	private final SqlParameterSource params;

	public QueryAndParams(String query, SqlParameterSource params) {
		this.query = query;
		this.params = params;
	}

	public String getQuery() {
		return query;
	}

	public SqlParameterSource getParams() {
		return params;
	}
}
