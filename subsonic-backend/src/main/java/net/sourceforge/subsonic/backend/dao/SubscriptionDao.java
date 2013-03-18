package net.sourceforge.subsonic.backend.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import net.sourceforge.subsonic.backend.domain.ProcessingStatus;
import net.sourceforge.subsonic.backend.domain.Subscription;

/**
 * Provides database services for PayPal subscriptions.
 *
 * @author Sindre Mehus
 */
public class SubscriptionDao extends AbstractDao {

    private static final Logger LOG = Logger.getLogger(SubscriptionDao.class);
    private static final String COLUMNS = "id, subscr_id, payer_id, btn_id, email, first_name, last_name, country, " +
            "amount, currency, valid_from, valid_to, processing_status, created, updated";

    private RowMapper subscriptionRowMapper = new SubscriptionRowMapper();

    /**
     * Returns the subscription with the given email.
     *
     * @param email The email.
     * @return The subscription or <code>null</code> if not found.
     */
    public Subscription getSubscriptionByEmail(String email) {
        if (email == null) {
            return null;
        }
        String sql = "select " + COLUMNS + " from subscription where email=?";
        return queryOne(sql, subscriptionRowMapper, email.toLowerCase());
    }

    /**
     * Returns all subscriptions with the given processing status.
     *
     * @param status The status.
     * @return List of subscriptions.
     */
    public List<Subscription> getSubscriptionsByProcessingStatus(ProcessingStatus status) {
        return query("select " + COLUMNS + " from subscription where processing_status=?", subscriptionRowMapper, status.name());
    }

    /**
     * Creates a new subscription.
     */
    public void createSubscription(Subscription s) {
        String sql = "insert into subscription (" + COLUMNS + ") values (" + questionMarks(COLUMNS) + ")";
        update(sql, null, s.getSubscrId(), s.getPayerId(), s.getBtnId(), StringUtils.lowerCase(s.getEmail()),
                s.getFirstName(), s.getLastName(), s.getCountry(), s.getAmount(), s.getCurrency(),
                s.getValidFrom(), s.getValidTo(), s.getProcessingStatus().name(), s.getCreated(), s.getUpdated());
        LOG.info("Created " + s);
    }

    /**
     * Updates the given subscription.
     */
    public void updateSubscription(Subscription s) {
        String sql = "update subscription set subscr_id=?, payer_id=?, btn_id=?, email=?, " +
                     "first_name=?, last_name=?, country=?, amount=?, currency=?, valid_from=?, " +
                     "valid_to=?, processing_status=?, created=?, updated=? where id=?";
        update(sql, s.getSubscrId(), s.getPayerId(), s.getBtnId(), s.getEmail(), s.getFirstName(), s.getLastName(),
                s.getCountry(), s.getAmount(), s.getCurrency(), s.getValidFrom(), s.getValidTo(),
                s.getProcessingStatus().name(), s.getCreated(), s.getUpdated(), s.getId());
        LOG.info("Updated " + s);
    }

    private static class SubscriptionRowMapper implements ParameterizedRowMapper<Subscription> {
        public Subscription mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Subscription(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
                               rs.getString(6), rs.getString(7), rs.getString(8), rs.getDouble(9), rs.getString(10),
                               rs.getTimestamp(11), rs.getTimestamp(12), ProcessingStatus.valueOf(rs.getString(13)),
                               rs.getTimestamp(14), rs.getTimestamp(15));
        }
    }
}