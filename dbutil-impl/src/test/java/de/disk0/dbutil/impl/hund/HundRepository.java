package de.disk0.dbutil.impl.hund;

import de.disk0.dbutil.impl.AbstractGuidRepository;

import javax.sql.DataSource;

public class HundRepository extends AbstractGuidRepository<Hund> {

    public HundRepository(DataSource dataSource) {
        super(dataSource);
    }

}
