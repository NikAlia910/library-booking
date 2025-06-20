import React, { useState, useEffect } from 'react';
import {
  Row,
  Col,
  Card,
  CardBody,
  CardTitle,
  Button,
  Input,
  FormGroup,
  Label,
  Badge,
  Alert,
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
} from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCalendarAlt, faChevronLeft, faChevronRight, faClock, faUser, faMapMarkerAlt } from '@fortawesome/free-solid-svg-icons';
import { TextFormat } from 'react-jhipster';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getResources } from 'app/entities/resource/resource.reducer';
import { getEntities as getReservations } from 'app/entities/reservation/reservation.reducer';
import { APP_DATE_FORMAT, APP_TIME_FORMAT, APP_DATE_ONLY_FORMAT } from 'app/config/constants';

interface CalendarEvent {
  id: string;
  title: string;
  start: Date;
  end: Date;
  resourceId: number;
  resourceTitle: string;
  resourceType: string;
  user: string;
}

export const AvailabilityCalendar = () => {
  const dispatch = useAppDispatch();

  const [selectedDate, setSelectedDate] = useState(new Date());
  const [selectedResource, setSelectedResource] = useState<number | null>(null);
  const [viewMode, setViewMode] = useState<'week' | 'day'>('week');
  const [selectedEvent, setSelectedEvent] = useState<CalendarEvent | null>(null);
  const [showEventModal, setShowEventModal] = useState(false);

  const resourceList = useAppSelector(state => state.resource.entities);
  const reservationList = useAppSelector(state => state.reservation.entities);
  const loading = useAppSelector(state => state.resource.loading);

  useEffect(() => {
    dispatch(getResources({}));
    dispatch(getReservations({}));
  }, [dispatch]);

  const getWeekDays = (date: Date) => {
    const week = [];
    const startOfWeek = new Date(date);
    const dayOfWeek = startOfWeek.getDay();
    const diff = startOfWeek.getDate() - dayOfWeek;
    startOfWeek.setDate(diff);

    for (let i = 0; i < 7; i++) {
      const currentDay = new Date(startOfWeek);
      currentDay.setDate(startOfWeek.getDate() + i);
      week.push(currentDay);
    }
    return week;
  };

  const getEventsForDate = (date: Date): CalendarEvent[] => {
    return (
      reservationList
        ?.filter(reservation => {
          if (!reservation.reservationDate) return false;
          const resDate = new Date(reservation.reservationDate);
          return resDate.toDateString() === date.toDateString();
        })
        .filter(reservation => {
          if (!selectedResource) return true;
          return reservation.resource?.id === selectedResource;
        })
        .map(reservation => ({
          id: reservation.id?.toString() || '',
          title: reservation.resource?.title || 'Unknown Resource',
          start: new Date(reservation.startTime || ''),
          end: new Date(reservation.endTime || ''),
          resourceId: reservation.resource?.id || 0,
          resourceTitle: reservation.resource?.title || 'Unknown',
          resourceType: reservation.resource?.resourceType || 'UNKNOWN',
          user: `${reservation.user?.firstName || ''} ${reservation.user?.lastName || ''}`.trim() || 'Unknown User',
        })) || []
    );
  };

  const getTimeSlots = () => {
    const slots = [];
    for (let hour = 8; hour <= 20; hour++) {
      slots.push(`${hour.toString().padStart(2, '0')}:00`);
    }
    return slots;
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

  const navigateWeek = (direction: 'prev' | 'next') => {
    const newDate = new Date(selectedDate);
    newDate.setDate(selectedDate.getDate() + (direction === 'next' ? 7 : -7));
    setSelectedDate(newDate);
  };

  const navigateDay = (direction: 'prev' | 'next') => {
    const newDate = new Date(selectedDate);
    newDate.setDate(selectedDate.getDate() + (direction === 'next' ? 1 : -1));
    setSelectedDate(newDate);
  };

  const handleEventClick = (event: CalendarEvent) => {
    setSelectedEvent(event);
    setShowEventModal(true);
  };

  const weekDays = getWeekDays(selectedDate);
  const timeSlots = getTimeSlots();

  return (
    <div className="availability-calendar">
      <Card className="border-0 shadow-sm">
        <CardBody>
          <div className="d-flex justify-content-between align-items-center mb-4">
            <CardTitle tag="h4" className="mb-0">
              <FontAwesomeIcon icon={faCalendarAlt} className="me-2" />
              Resource Availability Calendar
            </CardTitle>
            <div className="d-flex gap-2">
              <Button color={viewMode === 'day' ? 'primary' : 'outline-primary'} size="sm" onClick={() => setViewMode('day')}>
                Day View
              </Button>
              <Button color={viewMode === 'week' ? 'primary' : 'outline-primary'} size="sm" onClick={() => setViewMode('week')}>
                Week View
              </Button>
            </div>
          </div>

          {/* Controls */}
          <Row className="mb-4">
            <Col md="6">
              <FormGroup>
                <Label for="resourceFilter">Filter by Resource</Label>
                <Input
                  type="select"
                  id="resourceFilter"
                  value={selectedResource || ''}
                  onChange={e => setSelectedResource(e.target.value ? parseInt(e.target.value, 10) : null)}
                >
                  <option value="">All Resources</option>
                  {resourceList?.map(resource => (
                    <option key={resource.id} value={resource.id}>
                      {resource.title} ({resource.resourceType?.replace('_', ' ')})
                    </option>
                  ))}
                </Input>
              </FormGroup>
            </Col>
            <Col md="6">
              <Label>Navigation</Label>
              <div className="d-flex align-items-center gap-2">
                <Button
                  color="outline-secondary"
                  size="sm"
                  onClick={() => (viewMode === 'week' ? navigateWeek('prev') : navigateDay('prev'))}
                >
                  <FontAwesomeIcon icon={faChevronLeft} />
                </Button>
                <span className="mx-3 fw-bold">
                  {viewMode === 'week'
                    ? `Week of ${weekDays[0].toLocaleDateString()} - ${weekDays[6].toLocaleDateString()}`
                    : selectedDate.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                </span>
                <Button
                  color="outline-secondary"
                  size="sm"
                  onClick={() => (viewMode === 'week' ? navigateWeek('next') : navigateDay('next'))}
                >
                  <FontAwesomeIcon icon={faChevronRight} />
                </Button>
                <Button color="primary" size="sm" onClick={() => setSelectedDate(new Date())} className="ms-2">
                  Today
                </Button>
              </div>
            </Col>
          </Row>

          {loading ? (
            <div className="text-center py-4">
              <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
            </div>
          ) : (
            <div className="calendar-grid">
              {viewMode === 'week' ? (
                // Week View
                <div className="table-responsive">
                  <table className="table table-bordered">
                    <thead>
                      <tr>
                        <th style={{ width: '100px' }}>Time</th>
                        {weekDays.map(day => (
                          <th key={day.toISOString()} className="text-center">
                            <div>{day.toLocaleDateString('en-US', { weekday: 'short' })}</div>
                            <div className="text-muted small">{day.getDate()}</div>
                          </th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {timeSlots.map(time => (
                        <tr key={time}>
                          <td className="bg-light fw-bold text-center">{time}</td>
                          {weekDays.map(day => {
                            const dayEvents = getEventsForDate(day);
                            const timeEvents = dayEvents.filter(event => {
                              const eventHour = event.start.getHours();
                              const slotHour = parseInt(time.split(':')[0], 10);
                              return eventHour === slotHour;
                            });

                            return (
                              <td key={`${day.toISOString()}-${time}`} className="position-relative" style={{ height: '60px' }}>
                                {timeEvents.map(event => (
                                  <div
                                    key={event.id}
                                    className={`position-absolute w-100 cursor-pointer rounded p-1 text-white bg-${getResourceTypeColor(event.resourceType)}`}
                                    style={{ top: '2px', left: '2px', right: '2px', fontSize: '0.75rem' }}
                                    onClick={() => handleEventClick(event)}
                                    title={`${event.title} - ${event.user}`}
                                  >
                                    <div className="fw-bold">{event.title}</div>
                                    <div className="small">{event.user}</div>
                                  </div>
                                ))}
                              </td>
                            );
                          })}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                // Day View
                <div>
                  <h5 className="mb-3">
                    {selectedDate.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                  </h5>
                  {timeSlots.map(time => {
                    const dayEvents = getEventsForDate(selectedDate);
                    const timeEvents = dayEvents.filter(event => {
                      const eventHour = event.start.getHours();
                      const slotHour = parseInt(time.split(':')[0], 10);
                      return eventHour === slotHour;
                    });

                    return (
                      <Card key={time} className="mb-2 border-start border-primary border-3">
                        <CardBody className="py-2">
                          <Row className="align-items-center">
                            <Col md="2">
                              <strong>{time}</strong>
                            </Col>
                            <Col md="10">
                              {timeEvents.length > 0 ? (
                                <Row>
                                  {timeEvents.map(event => (
                                    <Col md="6" key={event.id}>
                                      <Card
                                        className={`mb-1 border-0 bg-${getResourceTypeColor(event.resourceType)} text-white cursor-pointer`}
                                        onClick={() => handleEventClick(event)}
                                      >
                                        <CardBody className="py-2">
                                          <div className="fw-bold">{event.title}</div>
                                          <small>
                                            <FontAwesomeIcon icon={faUser} className="me-1" />
                                            {event.user}
                                          </small>
                                        </CardBody>
                                      </Card>
                                    </Col>
                                  ))}
                                </Row>
                              ) : (
                                <span className="text-muted">Available</span>
                              )}
                            </Col>
                          </Row>
                        </CardBody>
                      </Card>
                    );
                  })}
                </div>
              )}
            </div>
          )}

          {/* Legend */}
          <div className="mt-4">
            <h6>Resource Types:</h6>
            <div className="d-flex gap-3">
              <Badge color="primary">Books</Badge>
              <Badge color="success">Meeting Rooms</Badge>
              <Badge color="warning">Equipment</Badge>
            </div>
          </div>
        </CardBody>
      </Card>

      {/* Event Detail Modal */}
      <Modal isOpen={showEventModal} toggle={() => setShowEventModal(false)}>
        <ModalHeader toggle={() => setShowEventModal(false)}>Reservation Details</ModalHeader>
        <ModalBody>
          {selectedEvent && (
            <div>
              <h5>{selectedEvent.title}</h5>
              <div className="mb-2">
                <FontAwesomeIcon icon={faUser} className="me-2" />
                <strong>Reserved by:</strong> {selectedEvent.user}
              </div>
              <div className="mb-2">
                <FontAwesomeIcon icon={faClock} className="me-2" />
                <strong>Time:</strong> <TextFormat value={selectedEvent.start} type="date" format={APP_DATE_FORMAT} />
                {' - '}
                <TextFormat value={selectedEvent.end} type="date" format={APP_DATE_FORMAT} />
              </div>
              <div className="mb-2">
                <FontAwesomeIcon icon={faMapMarkerAlt} className="me-2" />
                <strong>Type:</strong>
                <Badge color={getResourceTypeColor(selectedEvent.resourceType)} className="ms-2">
                  {selectedEvent.resourceType.replace('_', ' ')}
                </Badge>
              </div>
            </div>
          )}
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={() => setShowEventModal(false)}>
            Close
          </Button>
        </ModalFooter>
      </Modal>
    </div>
  );
};

export default AvailabilityCalendar;
