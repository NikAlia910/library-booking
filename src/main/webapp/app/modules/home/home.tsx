import './home.scss';

import React, { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import { Alert, Col, Row, Button, Card, CardBody, CardTitle } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBook, faCalendarAlt, faUsers, faCog } from '@fortawesome/free-solid-svg-icons';

import { useAppSelector } from 'app/config/store';

export const Home = () => {
  const account = useAppSelector(state => state.authentication.account);
  const navigate = useNavigate();

  useEffect(() => {
    // Redirect authenticated users to library dashboard
    if (account?.login) {
      navigate('/library/dashboard');
    }
  }, [account, navigate]);

  return (
    <Row>
      <Col md="3" className="pad">
        <span className="hipster rounded" />
      </Col>
      <Col md="9">
        <h1 className="display-4">Welcome to Library Booking System!</h1>
        <p className="lead">Your comprehensive solution for managing library resources and reservations</p>
        {account?.login ? (
          <div>
            <Alert color="success">You are logged in as user &quot;{account.login}&quot;. Redirecting to dashboard...</Alert>
          </div>
        ) : (
          <div>
            <Alert color="info">
              Welcome to our Library Booking System! Please
              <span>&nbsp;</span>
              <Link to="/login" className="alert-link">
                sign in
              </Link>
              &nbsp;to access the library resources.
              <br />
              Demo accounts: Admin (admin/admin) or User (user/user)
            </Alert>

            <Alert color="primary">
              New to our library?&nbsp;
              <Link to="/account/register" className="alert-link">
                Register a new account
              </Link>
            </Alert>

            {/* Feature Cards */}
            <Row className="mt-4">
              <Col md="6" className="mb-3">
                <Card className="border-0 shadow-sm h-100">
                  <CardBody className="text-center">
                    <FontAwesomeIcon icon={faBook} size="2x" className="text-primary mb-3" />
                    <CardTitle tag="h5">Resource Discovery</CardTitle>
                    <p className="text-muted">Search and discover books, meeting rooms, and equipment with our advanced search features.</p>
                  </CardBody>
                </Card>
              </Col>
              <Col md="6" className="mb-3">
                <Card className="border-0 shadow-sm h-100">
                  <CardBody className="text-center">
                    <FontAwesomeIcon icon={faCalendarAlt} size="2x" className="text-success mb-3" />
                    <CardTitle tag="h5">Easy Booking</CardTitle>
                    <p className="text-muted">
                      Make reservations with our intuitive calendar interface and real-time availability checking.
                    </p>
                  </CardBody>
                </Card>
              </Col>
              <Col md="6" className="mb-3">
                <Card className="border-0 shadow-sm h-100">
                  <CardBody className="text-center">
                    <FontAwesomeIcon icon={faUsers} size="2x" className="text-warning mb-3" />
                    <CardTitle tag="h5">User-Friendly</CardTitle>
                    <p className="text-muted">Designed with accessibility and ease-of-use in mind for all library patrons.</p>
                  </CardBody>
                </Card>
              </Col>
              <Col md="6" className="mb-3">
                <Card className="border-0 shadow-sm h-100">
                  <CardBody className="text-center">
                    <FontAwesomeIcon icon={faCog} size="2x" className="text-info mb-3" />
                    <CardTitle tag="h5">Smart Rules</CardTitle>
                    <p className="text-muted">Intelligent business rules ensure fair access and prevent conflicts in bookings.</p>
                  </CardBody>
                </Card>
              </Col>
            </Row>

            <div className="text-center mt-4">
              <Button color="primary" size="lg" tag={Link} to="/login">
                <FontAwesomeIcon icon={faBook} className="me-2" />
                Get Started
              </Button>
            </div>
          </div>
        )}
      </Col>
    </Row>
  );
};

export default Home;
