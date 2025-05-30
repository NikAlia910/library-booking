import './home.scss';

import React from 'react';
import { Link } from 'react-router-dom';
import { Alert, Col, Row, Card, CardBody, CardTitle, Button } from 'reactstrap';
import { useAppSelector } from 'app/config/store';

export const Home = () => {
  const account = useAppSelector(state => state.authentication.account);

  return (
    <Row>
      <Col md="12">
        <h1 className="display-4">Welcome to Library Booking System</h1>
        <p className="lead">Find and reserve your resources easily</p>
        {account?.login ? (
          <div>
            <Alert color="success">Welcome back, {account.login}!</Alert>
            <Row className="mt-4">
              <Col md="4">
                <Card>
                  <CardBody>
                    <CardTitle tag="h5">Search Resources</CardTitle>
                    <p>Search for books, meeting rooms, and equipment by title, author, or keywords.</p>
                    <Link to="/resource">
                      <Button color="primary">Browse Resources</Button>
                    </Link>
                  </CardBody>
                </Card>
              </Col>
              <Col md="4">
                <Card>
                  <CardBody>
                    <CardTitle tag="h5">My Reservations</CardTitle>
                    <p>View and manage your current reservations. You can have up to 5 active reservations.</p>
                    <Link to="/reservation">
                      <Button color="info">View Reservations</Button>
                    </Link>
                  </CardBody>
                </Card>
              </Col>
              <Col md="4">
                <Card>
                  <CardBody>
                    <CardTitle tag="h5">Quick Book</CardTitle>
                    <p>Make a new reservation for available resources. Book up to 30 days in advance.</p>
                    <Link to="/reservation/new">
                      <Button color="success">New Reservation</Button>
                    </Link>
                  </CardBody>
                </Card>
              </Col>
            </Row>
            <Row className="mt-4">
              <Col md="12">
                <Card>
                  <CardBody>
                    <CardTitle tag="h5">System Features</CardTitle>
                    <ul className="list-unstyled">
                      <li>✓ Search resources by title, author, keyword, or type</li>
                      <li>✓ View resource availability calendar</li>
                      <li>✓ Make reservations up to 30 days in advance</li>
                      <li>✓ Receive confirmation emails for reservations</li>
                      <li>✓ Maximum 5 active reservations per user</li>
                      <li>✓ Prevent overlapping reservations</li>
                    </ul>
                  </CardBody>
                </Card>
              </Col>
            </Row>
          </div>
        ) : (
          <div>
            <Alert color="warning">
              Please{' '}
              <Link to="/login" className="alert-link">
                sign in
              </Link>{' '}
              to make reservations.
              <br />
              New users can{' '}
              <Link to="/account/register" className="alert-link">
                register here
              </Link>
              .
            </Alert>
            <Card className="mt-4">
              <CardBody>
                <CardTitle tag="h5">About Our Library Booking System</CardTitle>
                <p>Our library booking system allows you to:</p>
                <ul>
                  <li>Search and reserve books, meeting rooms, and equipment</li>
                  <li>View real-time availability of resources</li>
                  <li>Manage your reservations online</li>
                  <li>Receive email confirmations</li>
                </ul>
                <p>Sign up now to start using our services!</p>
              </CardBody>
            </Card>
          </div>
        )}
      </Col>
    </Row>
  );
};

export default Home;
