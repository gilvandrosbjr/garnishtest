/*
 * Copyright 2016-2018, Garnish.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.garnishtest.modules.it.test_utils_dbunit.insert;

import org.garnishtest.modules.generic.db_util.jdbc_transactions.DatabaseAction;
import org.garnishtest.modules.generic.db_util.jdbc_transactions.SimpleTransactionTemplate;
import org.garnishtest.modules.generic.variables_resolver.VariablesResolver;
import org.garnishtest.modules.it.test_utils_dbunit.compare.utils.DbUnitXmlDataSetUtils;
import org.garnishtest.modules.it.test_utils_dbunit.config.DbUnitConfigurer;
import lombok.NonNull;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public final class DbUnitInserter {

    @NonNull private final SimpleTransactionTemplate transactionTemplate;
    @NonNull private final DbUnitConfigurer dbUnitConfigurer;

    public DbUnitInserter(@NonNull final DataSource dataSource,
                          @NonNull final DbUnitConfigurer dbUnitConfigurer) {
        this.transactionTemplate = new SimpleTransactionTemplate(dataSource);
        this.dbUnitConfigurer = dbUnitConfigurer;
    }

    public void insert(@NonNull final Resource dataSetResource,
                       @NonNull final VariablesResolver variablesResolver) throws DbUnitInserterException {
        if (!dataSetResource.exists()) {
            throw new IllegalArgumentException("cannot find dataSetResource [" + dataSetResource + "]");
        }
        if (!dataSetResource.isReadable()) {
            throw new IllegalArgumentException("cannot read dataSetResource [" + dataSetResource + "]");
        }

        try {
            tryToInsert(dataSetResource, variablesResolver);
        } catch (final Exception e) {
            throw new DbUnitInserterException("failed to insert to database the data from [" + dataSetResource + "]", e);
        }
    }

    private void tryToInsert(@NonNull final Resource datasetResource,
                             @NonNull final VariablesResolver variablesResolver) throws IOException, DatabaseUnitException, SQLException {
        final IDataSet dataSet = DbUnitXmlDataSetUtils.loadFromResource(datasetResource, variablesResolver);

        this.transactionTemplate.executeInTransaction(new DatabaseAction() {
            @Override
            public void doInTransaction(final Connection connection) throws DatabaseUnitException, SQLException {
                doInsertWithConnection(connection, dataSet);
            }
        });

    }

    private void doInsertWithConnection(@NonNull final Connection connection,
                                        @NonNull final IDataSet dataSet) throws DatabaseUnitException, SQLException {
        final DatabaseConnection databaseConnection = new DatabaseConnection(connection, this.dbUnitConfigurer.getDatabaseName());

        this.dbUnitConfigurer.configure(
                databaseConnection.getConfig()
        );

        DatabaseOperation.INSERT.execute(databaseConnection, dataSet);
    }

}
