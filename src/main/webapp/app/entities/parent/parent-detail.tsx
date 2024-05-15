import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './parent.reducer';

export const ParentDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const parentEntity = useAppSelector(state => state.parent.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="parentDetailsHeading">
          <Translate contentKey="sysParentApp.parent.detail.title">Parent</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{parentEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="sysParentApp.parent.name">Name</Translate>
            </span>
          </dt>
          <dd>{parentEntity.name}</dd>
          <dt>
            <span id="phone">
              <Translate contentKey="sysParentApp.parent.phone">Phone</Translate>
            </span>
          </dt>
          <dd>{parentEntity.phone}</dd>
          <dt>
            <span id="email">
              <Translate contentKey="sysParentApp.parent.email">Email</Translate>
            </span>
          </dt>
          <dd>{parentEntity.email}</dd>
          <dt>
            <span id="address">
              <Translate contentKey="sysParentApp.parent.address">Address</Translate>
            </span>
          </dt>
          <dd>{parentEntity.address}</dd>
        </dl>
        <Button tag={Link} to="/parent" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/parent/${parentEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ParentDetail;
