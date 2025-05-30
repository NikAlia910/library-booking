import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Row, Col, Card, CardBody, CardTitle, Button, Input, FormGroup, Label, Alert, Badge } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBook, faUsers, faCalendarAlt, faSearch, faPlus, faClock, faMapMarkerAlt } from '@fortawesome/free-solid-svg-icons';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getResources } from 'app/entities/resource/resource.reducer';
import { getEntities as getReservations } from 'app/entities/reservation/reservation.reducer';

export const LibraryDashboard = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const [searchTerm, setSearchTerm] = useState('');
  const [searchType, setSearchType] = useState('title');
  const [selectedResourceType, setSelectedResourceType] = useState('');

  const account = useAppSelector(state => state.authentication.account);
  const resourceList = useAppSelector(state => state.resource.entities);
  const reservationList = useAppSelector(state => state.reservation.entities);
  const loading = useAppSelector(state => state.resource.loading);

  useEffect(() => {
    dispatch(getResources({}));
    dispatch(getReservations({}));
  }, [dispatch]);

  const handleSearch = () => {
    if (searchTerm.trim()) {
      // This would call the new search endpoints we created
      // For now, we'll filter locally until the integration is complete
      // eslint-disable-next-line no-console
      console.log(`Searching for ${searchTerm} by ${searchType}`);
    }
  };

  const filteredResources = resourceList
    ?.filter(resource => {
      if (!searchTerm) return true;

      const searchLower = searchTerm.toLowerCase();
      switch (searchType) {
        case 'title':
          return resource.title?.toLowerCase().includes(searchLower);
        case 'author':
          return resource.author?.toLowerCase().includes(searchLower);
        case 'keywords':
          return resource.keywords?.toLowerCase().includes(searchLower);
        default:
          return true;
      }
    })
    .filter(resource => {
      if (!selectedResourceType) return true;
      return resource.resourceType === selectedResourceType;
    });

  const userReservations = reservationList?.filter(reservation => reservation.user?.id === account?.id) || [];

  const getResourceTypeIcon = (type: string) => {
    switch (type) {
      case 'BOOK':
        return faBook;
      case 'MEETING_ROOM':
        return faUsers;
      case 'EQUIPMENT':
        return faClock;
      default:
        return faBook;
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

  return (
    <div className="library-dashboard">
      {/* Hero Section */}
      <div className="bg-primary text-white p-5 mb-4 rounded">
        <Row className="align-items-center">
          <Col md="8">
            <h1 className="display-4 fw-bold">Library Resource Booking</h1>
            <p className="lead">
              Discover, reserve, and manage your library resources with ease. Search through books, meeting rooms, and equipment to find
              exactly what you need.
            </p>
          </Col>
          <Col md="4" className="text-end">
            <div className="d-grid gap-2">
              <Button color="light" size="lg" onClick={() => navigate('/reservation/new')} className="shadow">
                <FontAwesomeIcon icon={faPlus} className="me-2" />
                New Reservation
              </Button>
              <Button color="secondary" outline onClick={() => navigate('/library/calendar')}>
                <FontAwesomeIcon icon={faCalendarAlt} className="me-2" />
                View Calendar
              </Button>
            </div>
          </Col>
        </Row>
      </div>

      {/* Quick Stats */}
      <Row className="mb-4">
        <Col md="3">
          <Card className="text-center border-0 shadow-sm">
            <CardBody>
              <FontAwesomeIcon icon={faBook} size="2x" className="text-primary mb-2" />
              <h5 className="fw-bold">{resourceList?.length || 0}</h5>
              <small className="text-muted">Total Resources</small>
            </CardBody>
          </Card>
        </Col>
        <Col md="3">
          <Card className="text-center border-0 shadow-sm">
            <CardBody>
              <FontAwesomeIcon icon={faCalendarAlt} size="2x" className="text-success mb-2" />
              <h5 className="fw-bold">{userReservations.length}</h5>
              <small className="text-muted">Your Active Reservations</small>
            </CardBody>
          </Card>
        </Col>
        <Col md="3">
          <Card className="text-center border-0 shadow-sm">
            <CardBody>
              <FontAwesomeIcon icon={faUsers} size="2x" className="text-warning mb-2" />
              <h5 className="fw-bold">{resourceList?.filter(r => r.resourceType === 'MEETING_ROOM').length || 0}</h5>
              <small className="text-muted">Meeting Rooms</small>
            </CardBody>
          </Card>
        </Col>
        <Col md="3">
          <Card className="text-center border-0 shadow-sm">
            <CardBody>
              <FontAwesomeIcon icon={faClock} size="2x" className="text-info mb-2" />
              <h5 className="fw-bold">{resourceList?.filter(r => r.resourceType === 'EQUIPMENT').length || 0}</h5>
              <small className="text-muted">Equipment Available</small>
            </CardBody>
          </Card>
        </Col>
      </Row>

      {/* Search Section */}
      <Card className="mb-4 border-0 shadow-sm">
        <CardBody>
          <CardTitle tag="h4" className="mb-3">
            <FontAwesomeIcon icon={faSearch} className="me-2" />
            Search Resources
          </CardTitle>
          <Row>
            <Col md="4">
              <FormGroup>
                <Label for="searchType">Search By</Label>
                <Input type="select" id="searchType" value={searchType} onChange={e => setSearchType(e.target.value)}>
                  <option value="title">Title</option>
                  <option value="author">Author</option>
                  <option value="keywords">Keywords</option>
                </Input>
              </FormGroup>
            </Col>
            <Col md="4">
              <FormGroup>
                <Label for="resourceType">Resource Type</Label>
                <Input type="select" id="resourceType" value={selectedResourceType} onChange={e => setSelectedResourceType(e.target.value)}>
                  <option value="">All Types</option>
                  <option value="BOOK">Books</option>
                  <option value="MEETING_ROOM">Meeting Rooms</option>
                  <option value="EQUIPMENT">Equipment</option>
                </Input>
              </FormGroup>
            </Col>
            <Col md="4">
              <FormGroup>
                <Label for="searchTerm">Search Term</Label>
                <div className="d-flex">
                  <Input
                    type="text"
                    id="searchTerm"
                    placeholder="Enter search term..."
                    value={searchTerm}
                    onChange={e => setSearchTerm(e.target.value)}
                    onKeyPress={e => e.key === 'Enter' && handleSearch()}
                  />
                  <Button color="primary" className="ms-2" onClick={handleSearch}>
                    <FontAwesomeIcon icon={faSearch} />
                  </Button>
                </div>
              </FormGroup>
            </Col>
          </Row>
        </CardBody>
      </Card>

      {/* User's Recent Reservations */}
      {userReservations.length > 0 && (
        <Card className="mb-4 border-0 shadow-sm">
          <CardBody>
            <CardTitle tag="h4" className="mb-3">
              <FontAwesomeIcon icon={faCalendarAlt} className="me-2" />
              Your Recent Reservations
            </CardTitle>
            <Row>
              {userReservations.slice(0, 3).map(reservation => (
                <Col md="4" key={reservation.id}>
                  <Card className="mb-2 border-start border-primary border-3">
                    <CardBody className="py-2">
                      <h6 className="fw-bold mb-1">{reservation.resource?.title}</h6>
                      <small className="text-muted">
                        <FontAwesomeIcon icon={faCalendarAlt} className="me-1" />
                        {reservation.reservationDate ? new Date(reservation.reservationDate).toLocaleDateString() : 'N/A'}
                      </small>
                      <div className="mt-1">
                        <Badge color={getResourceTypeColor(reservation.resource?.resourceType || '')}>
                          {reservation.resource?.resourceType?.replace('_', ' ')}
                        </Badge>
                      </div>
                    </CardBody>
                  </Card>
                </Col>
              ))}
            </Row>
            <div className="text-end">
              <Link to="/reservation" className="btn btn-outline-primary">
                View All Reservations
              </Link>
            </div>
          </CardBody>
        </Card>
      )}

      {/* Resources Grid */}
      <Card className="border-0 shadow-sm">
        <CardBody>
          <CardTitle tag="h4" className="mb-3">
            Available Resources
            {searchTerm && (
              <small className="text-muted ms-2">
                ({filteredResources?.length || 0} results for &quot;{searchTerm}&quot;)
              </small>
            )}
          </CardTitle>

          {loading ? (
            <div className="text-center py-4">
              <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
            </div>
          ) : filteredResources && filteredResources.length > 0 ? (
            <Row>
              {filteredResources.slice(0, 12).map(resource => (
                <Col md="4" lg="3" key={resource.id} className="mb-3">
                  <Card className="h-100 border-0 shadow-sm resource-card">
                    <CardBody className="d-flex flex-column">
                      <div className="mb-2">
                        <FontAwesomeIcon
                          icon={getResourceTypeIcon(resource.resourceType || '')}
                          size="2x"
                          className={`text-${getResourceTypeColor(resource.resourceType || '')}`}
                        />
                      </div>
                      <h6 className="fw-bold mb-2">{resource.title}</h6>
                      {resource.author && <small className="text-muted mb-2">by {resource.author}</small>}
                      {resource.keywords && (
                        <small className="text-muted mb-2">
                          <FontAwesomeIcon icon={faMapMarkerAlt} className="me-1" />
                          {resource.keywords}
                        </small>
                      )}
                      <div className="mt-auto">
                        <Badge color={getResourceTypeColor(resource.resourceType || '')} className="mb-2">
                          {resource.resourceType?.replace('_', ' ')}
                        </Badge>
                        <div className="d-grid">
                          <Button color="primary" size="sm" onClick={() => navigate(`/resource/${resource.id}/reserve`)}>
                            Reserve Now
                          </Button>
                        </div>
                      </div>
                    </CardBody>
                  </Card>
                </Col>
              ))}
            </Row>
          ) : (
            <Alert color="info" className="text-center">
              <FontAwesomeIcon icon={faSearch} className="me-2" />
              {searchTerm ? 'No resources found matching your search criteria.' : 'No resources available at the moment.'}
            </Alert>
          )}

          {filteredResources && filteredResources.length > 12 && (
            <div className="text-center mt-3">
              <Link to="/resource" className="btn btn-outline-primary">
                View All Resources ({filteredResources.length})
              </Link>
            </div>
          )}
        </CardBody>
      </Card>
    </div>
  );
};

export default LibraryDashboard;
