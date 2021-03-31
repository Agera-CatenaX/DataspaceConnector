package de.fraunhofer.isst.dataspaceconnector.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.net.URI;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

/**
 * A ContractRule defines a rule that should be enforced.
 */
@Data
@Entity
@Table
@EqualsAndHashCode(callSuper = false)
@Setter(AccessLevel.PACKAGE)
public class ContractRule extends AbstractEntity {

    /**
     * Serial version uid.
     **/
    private static final long serialVersionUID = 1L;

    /**
     * The rule id on provider side.
     */
    private URI remoteId;

    /**
     * The title of the rule.
     */
    private String title;

    /**
     * The definition of the rule.
     **/
    @Column(length = 1024)
    private String value;

    /**
     * The contracts in which this rule is used.
     */
    @ManyToMany(mappedBy = "rules")
    private List<Contract> contracts;
}
