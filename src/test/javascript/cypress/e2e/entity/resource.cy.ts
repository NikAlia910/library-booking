import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('Resource e2e test', () => {
  const resourcePageUrl = '/resource';
  const resourcePageUrlPattern = new RegExp('/resource(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const resourceSample = { title: 'shout', resourceType: 'EQUIPMENT' };

  let resource;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/resources+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/resources').as('postEntityRequest');
    cy.intercept('DELETE', '/api/resources/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (resource) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/resources/${resource.id}`,
      }).then(() => {
        resource = undefined;
      });
    }
  });

  it('Resources menu should load Resources page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('resource');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Resource').should('exist');
    cy.url().should('match', resourcePageUrlPattern);
  });

  describe('Resource page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(resourcePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Resource page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/resource/new$'));
        cy.getEntityCreateUpdateHeading('Resource');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', resourcePageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/resources',
          body: resourceSample,
        }).then(({ body }) => {
          resource = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/resources+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/resources?page=0&size=20>; rel="last",<http://localhost/api/resources?page=0&size=20>; rel="first"',
              },
              body: [resource],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(resourcePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Resource page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('resource');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', resourcePageUrlPattern);
      });

      it('edit button click should load edit Resource page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Resource');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', resourcePageUrlPattern);
      });

      it('edit button click should load edit Resource page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Resource');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', resourcePageUrlPattern);
      });

      it('last delete button click should delete instance of Resource', () => {
        cy.intercept('GET', '/api/resources/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('resource').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', resourcePageUrlPattern);

        resource = undefined;
      });
    });
  });

  describe('new Resource page', () => {
    beforeEach(() => {
      cy.visit(`${resourcePageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Resource');
    });

    it('should create an instance of Resource', () => {
      cy.get(`[data-cy="title"]`).type('afore guide');
      cy.get(`[data-cy="title"]`).should('have.value', 'afore guide');

      cy.get(`[data-cy="author"]`).type('vain scarily object');
      cy.get(`[data-cy="author"]`).should('have.value', 'vain scarily object');

      cy.get(`[data-cy="keywords"]`).type('duh');
      cy.get(`[data-cy="keywords"]`).should('have.value', 'duh');

      cy.get(`[data-cy="resourceType"]`).select('EQUIPMENT');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        resource = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', resourcePageUrlPattern);
    });
  });
});
