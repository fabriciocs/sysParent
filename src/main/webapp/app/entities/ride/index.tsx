import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Ride from './ride';
import RideDetail from './ride-detail';
import RideUpdate from './ride-update';
import RideDeleteDialog from './ride-delete-dialog';

const RideRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Ride />} />
    <Route path="new" element={<RideUpdate />} />
    <Route path=":id">
      <Route index element={<RideDetail />} />
      <Route path="edit" element={<RideUpdate />} />
      <Route path="delete" element={<RideDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default RideRoutes;
