package com.radcortez.wow.auctions.batch.process.statistics;

import com.radcortez.wow.auctions.batch.process.AbstractAuctionFileProcess;
import org.apache.commons.dbutils.DbUtils;

import javax.annotation.Resource;
import javax.batch.api.chunk.ItemReader;
import javax.inject.Named;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Ivan St. Ivanov
 */
@Named
public class ProcessedAuctionsReader extends AbstractAuctionFileProcess implements ItemReader {
    @Resource(name = "java:comp/DefaultDataSource")
    protected DataSource dataSource;

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @Override
    public void open(Serializable checkpoint) throws Exception {
        connection = dataSource.getConnection();

        preparedStatement = connection.prepareStatement(
                        "SELECT" +
                        "   itemid as itemId," +
                        "   sum(quantity)," +
                        "   sum(bid)," +
                        "   sum(buyout)," +
                        "   min(bid / quantity)," +
                        "   min(buyout / quantity)," +
                        "   max(bid / quantity)," +
                        "   max(buyout / quantity)" +
                        " FROM auction" +
                        " WHERE auctionfile_id = " +
                        getContext().getFileToProcess().getId() +
                        " GROUP BY itemid" +
                        " ORDER BY 1",
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.HOLD_CURSORS_OVER_COMMIT
        );

        // Weird bug here. Check https://java.net/bugzilla/show_bug.cgi?id=5315
        //preparedStatement.setLong(1, getContext().getFileToProcess().getId());

        resultSet = preparedStatement.executeQuery();
    }

    @Override
    public void close() throws Exception {
        DbUtils.closeQuietly(resultSet);
        DbUtils.closeQuietly(preparedStatement);
        DbUtils.closeQuietly(connection);
    }

    @Override
    public Object readItem() throws Exception {
        return resultSet.next() ? resultSet : null;
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return null;
    }
}
