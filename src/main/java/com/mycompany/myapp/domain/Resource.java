package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.myapp.domain.enumeration.ResourceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Resource.
 */
@Entity
@Table(name = "resource")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Resource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Size(max = 255)
    @Column(name = "author", length = 255)
    private String author;

    @Size(max = 255)
    @Column(name = "keywords", length = 255)
    private String keywords;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "resource")
    @JsonIgnoreProperties(value = { "user", "resource" }, allowSetters = true)
    private Set<Reservation> reservations = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Resource id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public Resource title(String title) {
        this.setTitle(title);
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return this.author;
    }

    public Resource author(String author) {
        this.setAuthor(author);
        return this;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getKeywords() {
        return this.keywords;
    }

    public Resource keywords(String keywords) {
        this.setKeywords(keywords);
        return this;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public ResourceType getResourceType() {
        return this.resourceType;
    }

    public Resource resourceType(ResourceType resourceType) {
        this.setResourceType(resourceType);
        return this;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public Set<Reservation> getReservations() {
        return this.reservations;
    }

    public void setReservations(Set<Reservation> reservations) {
        if (this.reservations != null) {
            this.reservations.forEach(i -> i.setResource(null));
        }
        if (reservations != null) {
            reservations.forEach(i -> i.setResource(this));
        }
        this.reservations = reservations;
    }

    public Resource reservations(Set<Reservation> reservations) {
        this.setReservations(reservations);
        return this;
    }

    public Resource addReservation(Reservation reservation) {
        this.reservations.add(reservation);
        reservation.setResource(this);
        return this;
    }

    public Resource removeReservation(Reservation reservation) {
        this.reservations.remove(reservation);
        reservation.setResource(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Resource)) {
            return false;
        }
        return getId() != null && getId().equals(((Resource) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Resource{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", author='" + getAuthor() + "'" +
            ", keywords='" + getKeywords() + "'" +
            ", resourceType='" + getResourceType() + "'" +
            "}";
    }
}
