import React, { useState } from 'react';
import { Button, Card, CardBody, CardHeader, Form, FormGroup, Label } from 'reactstrap';
import { useAppDispatch } from 'app/config/store';
import { createEntity } from './reservation.reducer';
import { v4 as uuidv4 } from 'uuid';
import dayjs from 'dayjs';

export interface IReservationFormProps {
  resourceId: number;
  startTime: Date;
  endTime: Date;
  onClose: () => void;
}

export const ReservationForm = ({ resourceId, startTime, endTime, onClose }: IReservationFormProps) => {
  const dispatch = useAppDispatch();
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async event => {
    event.preventDefault();
    setSubmitting(true);

    const reservation = {
      reservationDate: dayjs(),
      startTime: dayjs(startTime),
      endTime: dayjs(endTime),
      reservationId: uuidv4(),
      resource: {
        id: resourceId,
      },
    };

    try {
      await dispatch(createEntity(reservation)).unwrap();
      onClose();
    } catch (error) {
      console.error('Error creating reservation:', error);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <h4 className="mb-0">Confirm Reservation</h4>
      </CardHeader>
      <CardBody>
        <Form onSubmit={handleSubmit}>
          <FormGroup>
            <Label>Start Time</Label>
            <div>{dayjs(startTime).format('MMMM D, YYYY h:mm A')}</div>
          </FormGroup>
          <FormGroup>
            <Label>End Time</Label>
            <div>{dayjs(endTime).format('MMMM D, YYYY h:mm A')}</div>
          </FormGroup>
          <div className="d-flex justify-content-end gap-2">
            <Button color="secondary" onClick={onClose} disabled={submitting}>
              Cancel
            </Button>
            <Button color="primary" type="submit" disabled={submitting}>
              {submitting ? 'Confirming...' : 'Confirm Reservation'}
            </Button>
          </div>
        </Form>
      </CardBody>
    </Card>
  );
};

export default ReservationForm;
