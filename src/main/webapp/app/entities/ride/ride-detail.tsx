import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './ride.reducer';

export const RideDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const rideEntity = useAppSelector(state => state.ride.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="rideDetailsHeading">
          <Translate contentKey="sysParentApp.ride.detail.title">Ride</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{rideEntity.id}</dd>
          <dt>
            <span id="scheduledTime">
              <Translate contentKey="sysParentApp.ride.scheduledTime">Scheduled Time</Translate>
            </span>
          </dt>
          <dd>{rideEntity.scheduledTime ? <TextFormat value={rideEntity.scheduledTime} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="sysParentApp.ride.status">Status</Translate>
            </span>
          </dt>
          <dd>{rideEntity.status}</dd>
          <dt>
            <span id="pickupAddress">
              <Translate contentKey="sysParentApp.ride.pickupAddress">Pickup Address</Translate>
            </span>
          </dt>
          <dd>{rideEntity.pickupAddress}</dd>
          <dt>
            <span id="dropoffAddress">
              <Translate contentKey="sysParentApp.ride.dropoffAddress">Dropoff Address</Translate>
            </span>
          </dt>
          <dd>{rideEntity.dropoffAddress}</dd>
          <dt>
            <Translate contentKey="sysParentApp.ride.child">Child</Translate>
          </dt>
          <dd>{rideEntity.child ? rideEntity.child.name : ''}</dd>
          <dt>
            <Translate contentKey="sysParentApp.ride.driver">Driver</Translate>
          </dt>
          <dd>{rideEntity.driver ? rideEntity.driver.name : ''}</dd>
        </dl>
        <Button tag={Link} to="/ride" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/ride/${rideEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default RideDetail;
