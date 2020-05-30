package de.disk0.dbutil.impl.katze;

import de.disk0.dbutil.impl.AbstractGuidRepository;

import javax.sql.DataSource;

public class KatzeRepository extends AbstractGuidRepository<Katze> {

    public KatzeRepository(DataSource dataSource) {
        super(dataSource);
    }

}
