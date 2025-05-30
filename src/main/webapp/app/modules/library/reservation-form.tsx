import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Row, Col, Card, CardBody, CardTitle, Button, Form, FormGroup, Label, Input, Alert, Badge, FormFeedback } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCalendarAlt, faClock, faUser, faBook, faCheckCircle, faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';
import dayjs from 'dayjs';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getResources, getEntity as getResource } from 'app/entities/resource/resource.reducer';
import { createEntity as createReservation } from 'app/entities/reservation/reservation.reducer';

interface FormData {
  resourceId: number;
  reservationDate: string;
  startTime: string;
  endTime: string;
  notes: string;
}

interface FormErrors {
  resourceId?: string;
  reservationDate?: string;
  startTime?: string;
  endTime?: string;
  general?: string;
}

export const ReservationForm = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { resourceId } = useParams<{ resourceId: string }>();

  const [formData, setFormData] = useState<FormData>({
    resourceId: resourceId ? parseInt(resourceId, 10) : 0,
    reservationDate: '',
    startTime: '',
    endTime: '',
    notes: '',
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);

  const account = useAppSelector(state => state.authentication.account);
  const resourceList = useAppSelector(state => state.resource.entities);
  const selectedResource = useAppSelector(state => state.resource.entity);
  const loading = useAppSelector(state => state.resource.loading);
  const updateSuccess = useAppSelector(state => state.reservation.updateSuccess);

  useEffect(() => {
    dispatch(getResources({}));
    if (resourceId) {
      dispatch(getResource(parseInt(resourceId, 10)));
    }
  }, [dispatch, resourceId]);

  useEffect(() => {
    if (updateSuccess) {
      setShowSuccess(true);
      setTimeout(() => {
        navigate('/library/dashboard');
      }, 2000);
    }
  }, [updateSuccess, navigate]);

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    if (!formData.resourceId || formData.resourceId === 0) {
      newErrors.resourceId = 'Please select a resource';
    }

    if (!formData.reservationDate) {
      newErrors.reservationDate = 'Please select a date';
    } else {
      const selectedDate = new Date(formData.reservationDate);
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      if (selectedDate < today) {
        newErrors.reservationDate = 'Cannot book for past dates';
      }

      // Check 30-day advance booking window
      const maxDate = new Date();
      maxDate.setDate(maxDate.getDate() + 30);
      if (selectedDate > maxDate) {
        newErrors.reservationDate = 'Cannot book more than 30 days in advance';
      }
    }

    if (!formData.startTime) {
      newErrors.startTime = 'Please select start time';
    }

    if (!formData.endTime) {
      newErrors.endTime = 'Please select end time';
    }

    if (formData.startTime && formData.endTime) {
      const start = new Date(`2000-01-01T${formData.startTime}:00`);
      const end = new Date(`2000-01-01T${formData.endTime}:00`);

      if (end <= start) {
        newErrors.endTime = 'End time must be after start time';
      }

      const durationHours = (end.getTime() - start.getTime()) / (1000 * 60 * 60);
      if (durationHours < 1) {
        newErrors.endTime = 'Minimum reservation duration is 1 hour';
      }

      if (durationHours > 8) {
        newErrors.endTime = 'Maximum reservation duration is 8 hours';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      const startDateTime = dayjs(`${formData.reservationDate}T${formData.startTime}:00`);
      const endDateTime = dayjs(`${formData.reservationDate}T${formData.endTime}:00`);

      const reservation = {
        resource: { id: formData.resourceId },
        user: { id: account?.id },
        reservationDate: startDateTime,
        startTime: startDateTime,
        endTime: endDateTime,
        reservationId: `RES-${Date.now()}`,
        notes: formData.notes,
      };

      await dispatch(createReservation(reservation));
    } catch (error) {
      setErrors({ general: 'Failed to create reservation. Please try again.' });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));

    // Clear specific error when user starts typing
    if (errors[name as keyof FormErrors]) {
      setErrors(prev => ({ ...prev, [name]: undefined }));
    }
  };

  const getResourceTypeColor = (type: string) => {
    switch (type) {
      case 'BOOK':
        return 'primary';
      case 'MEETING_ROOM':
        return 'success';
      case 'EQUIPMENT':
        return 'warning';
      default:
        return 'secondary';
    }
  };

  const generateTimeOptions = () => {
    const options = [];
    for (let hour = 8; hour <= 20; hour++) {
      for (let minute = 0; minute < 60; minute += 30) {
        const timeString = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
        options.push(timeString);
      }
    }
    return options;
  };

  const timeOptions = generateTimeOptions();

  if (showSuccess) {
    return (
      <div className="reservation-form">
        <Card className="border-0 shadow-sm">
          <CardBody className="text-center py-5">
            <FontAwesomeIcon icon={faCheckCircle} size="3x" className="text-success mb-3" />
            <h3 className="text-success">Reservation Successful!</h3>
            <p className="text-muted">Your reservation has been created successfully. Redirecting to dashboard...</p>
          </CardBody>
        </Card>
      </div>
    );
  }

  return (
    <div className="reservation-form">
      <Row>
        <Col lg="8" className="mx-auto">
          <Card className="border-0 shadow-sm">
            <CardBody>
              <CardTitle tag="h3" className="mb-4 text-center">
                <FontAwesomeIcon icon={faCalendarAlt} className="me-2" />
                Make a Reservation
              </CardTitle>

              {errors.general && (
                <Alert color="danger">
                  <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
                  {errors.general}
                </Alert>
              )}

              {/* Resource Preview */}
              {selectedResource && (
                <Card className="mb-4 border-start border-primary border-3 bg-light">
                  <CardBody className="py-3">
                    <Row className="align-items-center">
                      <Col md="8">
                        <h5 className="mb-1">{selectedResource.title}</h5>
                        {selectedResource.author && <p className="text-muted mb-1">by {selectedResource.author}</p>}
                        {selectedResource.keywords && <small className="text-muted">{selectedResource.keywords}</small>}
                      </Col>
                      <Col md="4" className="text-end">
                        <Badge color={getResourceTypeColor(selectedResource.resourceType || '')} className="fs-6">
                          {selectedResource.resourceType?.replace('_', ' ')}
                        </Badge>
                      </Col>
                    </Row>
                  </CardBody>
                </Card>
              )}

              <Form onSubmit={handleSubmit}>
                <Row>
                  <Col md="6">
                    <FormGroup>
                      <Label for="resourceId">
                        <FontAwesomeIcon icon={faBook} className="me-2" />
                        Select Resource *
                      </Label>
                      <Input
                        type="select"
                        name="resourceId"
                        id="resourceId"
                        value={formData.resourceId}
                        onChange={handleInputChange}
                        invalid={!!errors.resourceId}
                        disabled={!!resourceId}
                      >
                        <option value={0}>Select a resource...</option>
                        {resourceList?.map(resource => (
                          <option key={resource.id} value={resource.id}>
                            {resource.title} ({resource.resourceType?.replace('_', ' ')})
                          </option>
                        ))}
                      </Input>
                      {errors.resourceId && <FormFeedback>{errors.resourceId}</FormFeedback>}
                    </FormGroup>
                  </Col>

                  <Col md="6">
                    <FormGroup>
                      <Label for="reservationDate">
                        <FontAwesomeIcon icon={faCalendarAlt} className="me-2" />
                        Reservation Date *
                      </Label>
                      <Input
                        type="date"
                        name="reservationDate"
                        id="reservationDate"
                        value={formData.reservationDate}
                        onChange={handleInputChange}
                        invalid={!!errors.reservationDate}
                        min={new Date().toISOString().split('T')[0]}
                        max={new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]}
                      />
                      {errors.reservationDate && <FormFeedback>{errors.reservationDate}</FormFeedback>}
                    </FormGroup>
                  </Col>
                </Row>

                <Row>
                  <Col md="6">
                    <FormGroup>
                      <Label for="startTime">
                        <FontAwesomeIcon icon={faClock} className="me-2" />
                        Start Time *
                      </Label>
                      <Input
                        type="select"
                        name="startTime"
                        id="startTime"
                        value={formData.startTime}
                        onChange={handleInputChange}
                        invalid={!!errors.startTime}
                      >
                        <option value="">Select start time...</option>
                        {timeOptions.map(time => (
                          <option key={time} value={time}>
                            {time}
                          </option>
                        ))}
                      </Input>
                      {errors.startTime && <FormFeedback>{errors.startTime}</FormFeedback>}
                    </FormGroup>
                  </Col>

                  <Col md="6">
                    <FormGroup>
                      <Label for="endTime">
                        <FontAwesomeIcon icon={faClock} className="me-2" />
                        End Time *
                      </Label>
                      <Input
                        type="select"
                        name="endTime"
                        id="endTime"
                        value={formData.endTime}
                        onChange={handleInputChange}
                        invalid={!!errors.endTime}
                      >
                        <option value="">Select end time...</option>
                        {timeOptions.map(time => (
                          <option key={time} value={time}>
                            {time}
                          </option>
                        ))}
                      </Input>
                      {errors.endTime && <FormFeedback>{errors.endTime}</FormFeedback>}
                    </FormGroup>
                  </Col>
                </Row>

                <FormGroup>
                  <Label for="notes">Additional Notes (Optional)</Label>
                  <Input
                    type="textarea"
                    name="notes"
                    id="notes"
                    value={formData.notes}
                    onChange={handleInputChange}
                    rows={3}
                    placeholder="Any special requirements or notes for your reservation..."
                  />
                </FormGroup>

                {/* Business Rules Info */}
                <Alert color="info" className="mb-4">
                  <h6>Reservation Guidelines:</h6>
                  <ul className="mb-0">
                    <li>Maximum 5 active reservations per patron</li>
                    <li>Reservations can be made up to 30 days in advance</li>
                    <li>Minimum reservation duration: 1 hour</li>
                    <li>Maximum reservation duration: 8 hours</li>
                    <li>No overlapping reservations for the same resource</li>
                  </ul>
                </Alert>

                <div className="d-flex justify-content-between">
                  <Button type="button" color="secondary" onClick={() => navigate(-1)}>
                    Cancel
                  </Button>
                  <Button type="submit" color="primary" disabled={isSubmitting || loading} className="px-4">
                    {isSubmitting ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                        Creating...
                      </>
                    ) : (
                      <>
                        <FontAwesomeIcon icon={faCheckCircle} className="me-2" />
                        Create Reservation
                      </>
                    )}
                  </Button>
                </div>
              </Form>
            </CardBody>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default ReservationForm;
