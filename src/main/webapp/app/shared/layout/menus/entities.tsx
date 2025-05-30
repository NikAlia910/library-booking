import React from 'react';
import { NavDropdown } from './menu-components';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBook, faCalendar } from '@fortawesome/free-solid-svg-icons';

export const EntitiesMenu = () => (
  <NavDropdown icon="th-list" name="Resources" id="entity-menu" data-cy="entity" style={{ maxHeight: '80vh', overflow: 'auto' }}>
    <FontAwesomeIcon icon={faBook} /> <span>Resources</span>
    <FontAwesomeIcon icon={faCalendar} /> <span>Reservations</span>
  </NavDropdown>
);
