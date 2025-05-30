import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col, Alert } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntity } from './resource.reducer';
import { getEntities as getReservations } from '../reservation/reservation.reducer';
import ResourceCalendar from './resource-calendar';
import ReservationForm from '../reservation/reservation-form';
import dayjs from 'dayjs';

export const ResourceDetail = () => {
  const dispatch = useAppDispatch();
  const { id } = useParams<'id'>();
  const [showReservationForm, setShowReservationForm] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState<{ start: Date; end: Date } | null>(null);
  const [error, setError] = useState<string | null>(null);

  const resourceEntity = useAppSelector(state => state.resource.entity);
  const reservations = useAppSelector(state => state.reservation.entities);
  const account = useAppSelector(state => state.authentication.account);

  useEffect(() => {
    if (id) {
      dispatch(getEntity(id));
      dispatch(getReservations({}));
    }
  }, [id]);

  const handleSlotSelect = (start: Date, end: Date) => {
    // Check if the selected time is in the future
    if (dayjs(start).isBefore(dayjs())) {
      setError('Cannot make reservations in the past');
      return;
    }

    // Check if the selected time is within 30 days
    if (dayjs(start).isAfter(dayjs().add(30, 'day'))) {
      setError('Cannot make reservations more than 30 days in advance');
      return;
    }

    // Check for overlapping reservations
    const hasOverlap = reservations.some(
      reservation =>
        (dayjs(start).isBetween(reservation.startTime, reservation.endTime, null, '[]') ||
          dayjs(end).isBetween(reservation.startTime, reservation.endTime, null, '[]')) &&
        reservation.resource.id === resourceEntity.id,
    );

    if (hasOverlap) {
      setError('This time slot overlaps with an existing reservation');
      return;
    }

    // Check user's active reservation count
    const activeReservations = reservations.filter(
      reservation =>
        reservation.user?.login === account.login &&
        dayjs(reservation.endTime).isAfter(dayjs()) &&
        reservation.resource.id === resourceEntity.id,
    );

    if (activeReservations.length >= 5) {
      setError('You have reached the maximum limit of 5 active reservations');
      return;
    }

    setError(null);
    setSelectedSlot({ start, end });
    setShowReservationForm(true);
  };

  return (
    <div>
      <Row>
        <Col md="8">
          <h2>
            Resource Details
            <Button tag={Link} to="/resource" replace color="info" data-cy="entityDetailsBackButton" className="float-end">
              <FontAwesomeIcon icon="arrow-left" /> Back
            </Button>
          </h2>
        </Col>
      </Row>
      <Row className="mt-3">
        <Col md="8">
          <dl className="jh-entity-details">
            <dt>Title</dt>
            <dd>{resourceEntity.title}</dd>
            <dt>Author</dt>
            <dd>{resourceEntity.author}</dd>
            <dt>Keywords</dt>
            <dd>{resourceEntity.keywords}</dd>
            <dt>Resource Type</dt>
            <dd>{resourceEntity.resourceType}</dd>
          </dl>
        </Col>
      </Row>

      {error && (
        <Row className="mt-3">
          <Col>
            <Alert color="danger">{error}</Alert>
          </Col>
        </Row>
      )}

      <Row className="mt-3">
        <Col>
          <ResourceCalendar
            resource={resourceEntity}
            reservations={reservations.filter(r => r.resource?.id === resourceEntity.id)}
            onSelectSlot={handleSlotSelect}
          />
        </Col>
      </Row>

      {showReservationForm && selectedSlot && (
        <Row className="mt-3">
          <Col>
            <ReservationForm
              resourceId={resourceEntity.id}
              startTime={selectedSlot.start}
              endTime={selectedSlot.end}
              onClose={() => setShowReservationForm(false)}
            />
          </Col>
        </Row>
      )}
    </div>
  );
};

export default ResourceDetail;
