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

describe('Reservation e2e test', () => {
  const reservationPageUrl = '/reservation';
  const reservationPageUrlPattern = new RegExp('/reservation(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  // const reservationSample = {"reservationDate":"2025-05-29T18:13:57.474Z","startTime":"2025-05-30T05:10:15.518Z","endTime":"2025-05-29T23:38:47.976Z","reservationId":"raw hm"};

  let reservation;
  // let user;
  // let resource;

  beforeEach(() => {
    cy.login(username, password);
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/users',
      body: {"login":"Xs@Am\\cXh2bo\\adAb6A\\umzfW\\>-HT\\ROk","firstName":"Alek","lastName":"Johns","email":"Kristoffer48@gmail.com","imageUrl":"wisely instantly airbrush"},
    }).then(({ body }) => {
      user = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/resources',
      body: {"title":"generously shocked","author":"um","keywords":"tense squiggly micromanage","resourceType":"BOOK"},
    }).then(({ body }) => {
      resource = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/reservations+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/reservations').as('postEntityRequest');
    cy.intercept('DELETE', '/api/reservations/*').as('deleteEntityRequest');

    // Mock the users and resources API calls to allow the component to render
    cy.intercept('GET', '/api/users*', {
      statusCode: 200,
      body: [
        { id: 1, login: 'admin', firstName: 'Admin', lastName: 'User' },
        { id: 2, login: 'user', firstName: 'User', lastName: 'User' },
      ],
    }).as('usersRequest');

    cy.intercept('GET', '/api/resources*', {
      statusCode: 200,
      body: [
        { id: 1, title: 'Test Book', author: 'Test Author', resourceType: 'BOOK' },
        { id: 2, title: 'Test Room', resourceType: 'MEETING_ROOM' },
      ],
    }).as('resourcesRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/users', {
      statusCode: 200,
      body: [user],
    });

    cy.intercept('GET', '/api/resources', {
      statusCode: 200,
      body: [resource],
    });

  });
   */

  afterEach(() => {
    if (reservation) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/reservations/${reservation.id}`,
      }).then(() => {
        reservation = undefined;
      });
    }
  });

  /* Disabled due to incompatibility
  afterEach(() => {
    if (user) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/users/${user.id}`,
      }).then(() => {
        user = undefined;
      });
    }
    if (resource) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/resources/${resource.id}`,
      }).then(() => {
        resource = undefined;
      });
    }
  });
   */

  it('Reservations menu should load Reservations page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('reservation');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Reservation').should('exist');
    cy.url().should('match', reservationPageUrlPattern);
  });

  describe('Reservation page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(reservationPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Reservation page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/reservation/new$'));

        // Wait for the page to load and log what we see
        cy.wait(2000); // Give extra time for the page to load

        // Debug: Log the page content
        cy.get('body').then($body => {
          console.log('Page body:', $body.html());
        });

        // Try to find the heading with a longer timeout
        cy.get(`[data-cy="ReservationCreateUpdateHeading"]`, { timeout: 10000 }).should('be.visible');

        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', reservationPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/reservations',
          body: {
            ...reservationSample,
            user: user,
            resource: resource,
          },
        }).then(({ body }) => {
          reservation = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/reservations+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/reservations?page=0&size=20>; rel="last",<http://localhost/api/reservations?page=0&size=20>; rel="first"',
              },
              body: [reservation],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(reservationPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(reservationPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details Reservation page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('reservation');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', reservationPageUrlPattern);
      });

      it('edit button click should load edit Reservation page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Reservation');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', reservationPageUrlPattern);
      });

      it('edit button click should load edit Reservation page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Reservation');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', reservationPageUrlPattern);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of Reservation', () => {
        cy.intercept('GET', '/api/reservations/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('reservation').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', reservationPageUrlPattern);

        reservation = undefined;
      });
    });
  });

  describe('new Reservation page', () => {
    beforeEach(() => {
      cy.visit(`${reservationPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Reservation');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of Reservation', () => {
      cy.get(`[data-cy="reservationDate"]`).type('2025-05-29T22:25');
      cy.get(`[data-cy="reservationDate"]`).blur();
      cy.get(`[data-cy="reservationDate"]`).should('have.value', '2025-05-29T22:25');

      cy.get(`[data-cy="startTime"]`).type('2025-05-29T12:11');
      cy.get(`[data-cy="startTime"]`).blur();
      cy.get(`[data-cy="startTime"]`).should('have.value', '2025-05-29T12:11');

      cy.get(`[data-cy="endTime"]`).type('2025-05-29T09:04');
      cy.get(`[data-cy="endTime"]`).blur();
      cy.get(`[data-cy="endTime"]`).should('have.value', '2025-05-29T09:04');

      cy.get(`[data-cy="reservationId"]`).type('and whenever anenst');
      cy.get(`[data-cy="reservationId"]`).should('have.value', 'and whenever anenst');

      cy.get(`[data-cy="user"]`).select(1);
      cy.get(`[data-cy="resource"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        reservation = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', reservationPageUrlPattern);
    });
  });
});
