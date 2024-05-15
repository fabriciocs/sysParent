import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IChild } from 'app/shared/model/child.model';
import { getEntities as getChildren } from 'app/entities/child/child.reducer';
import { IDriver } from 'app/shared/model/driver.model';
import { getEntities as getDrivers } from 'app/entities/driver/driver.reducer';
import { IRide } from 'app/shared/model/ride.model';
import { RideStatus } from 'app/shared/model/enumerations/ride-status.model';
import { getEntity, updateEntity, createEntity, reset } from './ride.reducer';

export const RideUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const children = useAppSelector(state => state.child.entities);
  const drivers = useAppSelector(state => state.driver.entities);
  const rideEntity = useAppSelector(state => state.ride.entity);
  const loading = useAppSelector(state => state.ride.loading);
  const updating = useAppSelector(state => state.ride.updating);
  const updateSuccess = useAppSelector(state => state.ride.updateSuccess);
  const rideStatusValues = Object.keys(RideStatus);

  const handleClose = () => {
    navigate('/ride');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getChildren({}));
    dispatch(getDrivers({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  // eslint-disable-next-line complexity
  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }
    values.scheduledTime = convertDateTimeToServer(values.scheduledTime);

    const entity = {
      ...rideEntity,
      ...values,
      child: children.find(it => it.id.toString() === values.child?.toString()),
      driver: drivers.find(it => it.id.toString() === values.driver?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          scheduledTime: displayDefaultDateTime(),
        }
      : {
          status: 'SCHEDULED',
          ...rideEntity,
          scheduledTime: convertDateTimeFromServer(rideEntity.scheduledTime),
          child: rideEntity?.child?.id,
          driver: rideEntity?.driver?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="sysParentApp.ride.home.createOrEditLabel" data-cy="RideCreateUpdateHeading">
            <Translate contentKey="sysParentApp.ride.home.createOrEditLabel">Create or edit a Ride</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="ride-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('sysParentApp.ride.scheduledTime')}
                id="ride-scheduledTime"
                name="scheduledTime"
                data-cy="scheduledTime"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField label={translate('sysParentApp.ride.status')} id="ride-status" name="status" data-cy="status" type="select">
                {rideStatusValues.map(rideStatus => (
                  <option value={rideStatus} key={rideStatus}>
                    {translate('sysParentApp.RideStatus.' + rideStatus)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('sysParentApp.ride.pickupAddress')}
                id="ride-pickupAddress"
                name="pickupAddress"
                data-cy="pickupAddress"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  minLength: { value: 5, message: translate('entity.validation.minlength', { min: 5 }) },
                }}
              />
              <ValidatedField
                label={translate('sysParentApp.ride.dropoffAddress')}
                id="ride-dropoffAddress"
                name="dropoffAddress"
                data-cy="dropoffAddress"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  minLength: { value: 5, message: translate('entity.validation.minlength', { min: 5 }) },
                }}
              />
              <ValidatedField
                id="ride-child"
                name="child"
                data-cy="child"
                label={translate('sysParentApp.ride.child')}
                type="select"
                required
              >
                <option value="" key="0" />
                {children
                  ? children.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.name}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <ValidatedField
                id="ride-driver"
                name="driver"
                data-cy="driver"
                label={translate('sysParentApp.ride.driver')}
                type="select"
                required
              >
                <option value="" key="0" />
                {drivers
                  ? drivers.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.name}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/ride" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default RideUpdate;
