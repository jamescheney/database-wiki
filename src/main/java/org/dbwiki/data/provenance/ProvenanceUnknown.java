package org.dbwiki.data.provenance;

import org.dbwiki.user.User;

public class ProvenanceUnknown extends Provenance {
	public ProvenanceUnknown(User user) {
		super(ProvenanceTypeUnknown, user, null);
	}
	
	@Override
	public String name() {
		return "UNKNOWN";
	}
}
