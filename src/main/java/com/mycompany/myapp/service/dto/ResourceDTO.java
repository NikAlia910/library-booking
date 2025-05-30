package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.ResourceType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.Resource} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ResourceDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String title;

    @Size(max = 255)
    private String author;

    @Size(max = 255)
    private String keywords;

    @NotNull
    private ResourceType resourceType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourceDTO)) {
            return false;
        }

        ResourceDTO resourceDTO = (ResourceDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, resourceDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ResourceDTO{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", author='" + getAuthor() + "'" +
            ", keywords='" + getKeywords() + "'" +
            ", resourceType='" + getResourceType() + "'" +
            "}";
    }
}
