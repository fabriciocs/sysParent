import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Parent from './parent';
import Driver from './driver';
import Child from './child';
import Ride from './ride';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="parent/*" element={<Parent />} />
        <Route path="driver/*" element={<Driver />} />
        <Route path="child/*" element={<Child />} />
        <Route path="ride/*" element={<Ride />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
