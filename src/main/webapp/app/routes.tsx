import React from 'react';
import { Route } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import Loadable from 'react-loadable';

import Login from 'app/modules/login/login';
import Register from 'app/modules/account/register/register';
import Activate from 'app/modules/account/activate/activate';
import PasswordResetInit from 'app/modules/account/password-reset/init/password-reset-init';
import PasswordResetFinish from 'app/modules/account/password-reset/finish/password-reset-finish';
import Logout from 'app/modules/login/logout';
import Home from 'app/modules/home/home';
import EntitiesRoutes from 'app/entities/routes';
import PrivateRoute from 'app/shared/auth/private-route';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import PageNotFound from 'app/shared/error/page-not-found';
import { AUTHORITIES } from 'app/config/constants';
import { sendActivity } from 'app/config/websocket-middleware';

const loading = <div>loading ...</div>;

// Lazy load library components
const LibraryDashboard = Loadable({
  loader: () => import('app/modules/library/library-dashboard'),
  loading: () => loading,
});

const AvailabilityCalendar = Loadable({
  loader: () => import('app/modules/library/availability-calendar'),
  loading: () => loading,
});

const ReservationForm = Loadable({
  loader: () => import('app/modules/library/reservation-form'),
  loading: () => loading,
});

const Account = Loadable({
  loader: () => import('app/modules/account'),
  loading: () => loading,
});

const Admin = Loadable({
  loader: () => import('app/modules/administration'),
  loading: () => loading,
});

const AppRoutes = () => {
  const pageLocation = useLocation();
  React.useEffect(() => {
    sendActivity(pageLocation.pathname);
  }, [pageLocation]);
  return (
    <div className="view-routes">
      <ErrorBoundaryRoutes>
        <Route index element={<Home />} />
        <Route path="login" element={<Login />} />
        <Route path="logout" element={<Logout />} />
        <Route path="account">
          <Route path="register" element={<Register />} />
          <Route path="activate" element={<Activate />} />
          <Route path="reset">
            <Route path="request" element={<PasswordResetInit />} />
            <Route path="finish" element={<PasswordResetFinish />} />
          </Route>
          <Route
            path="*"
            element={
              <PrivateRoute hasAnyAuthorities={[AUTHORITIES.USER]}>
                <Account />
              </PrivateRoute>
            }
          />
        </Route>
        <Route
          path="admin/*"
          element={
            <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN]}>
              <Admin />
            </PrivateRoute>
          }
        />
        {/* Library Booking System Routes */}
        <Route
          path="library/dashboard"
          element={
            <PrivateRoute hasAnyAuthorities={[AUTHORITIES.USER]}>
              <LibraryDashboard />
            </PrivateRoute>
          }
        />
        <Route
          path="library/calendar"
          element={
            <PrivateRoute hasAnyAuthorities={[AUTHORITIES.USER]}>
              <AvailabilityCalendar />
            </PrivateRoute>
          }
        />
        <Route
          path="library/reservation/new"
          element={
            <PrivateRoute hasAnyAuthorities={[AUTHORITIES.USER]}>
              <ReservationForm />
            </PrivateRoute>
          }
        />
        <Route
          path="resource/:resourceId/reserve"
          element={
            <PrivateRoute hasAnyAuthorities={[AUTHORITIES.USER]}>
              <ReservationForm />
            </PrivateRoute>
          }
        />
        <Route
          path="/*"
          element={
            <PrivateRoute hasAnyAuthorities={[AUTHORITIES.USER]}>
              <EntitiesRoutes />
            </PrivateRoute>
          }
        />
        <Route path="*" element={<PageNotFound />} />
      </ErrorBoundaryRoutes>
    </div>
  );
};

export default AppRoutes;
