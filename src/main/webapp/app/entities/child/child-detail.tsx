import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './child.reducer';

export const ChildDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const childEntity = useAppSelector(state => state.child.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="childDetailsHeading">
          <Translate contentKey="sysParentApp.child.detail.title">Child</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{childEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="sysParentApp.child.name">Name</Translate>
            </span>
          </dt>
          <dd>{childEntity.name}</dd>
          <dt>
            <span id="age">
              <Translate contentKey="sysParentApp.child.age">Age</Translate>
            </span>
          </dt>
          <dd>{childEntity.age}</dd>
          <dt>
            <span id="schoolName">
              <Translate contentKey="sysParentApp.child.schoolName">School Name</Translate>
            </span>
          </dt>
          <dd>{childEntity.schoolName}</dd>
          <dt>
            <Translate contentKey="sysParentApp.child.parent">Parent</Translate>
          </dt>
          <dd>{childEntity.parent ? childEntity.parent.name : ''}</dd>
        </dl>
        <Button tag={Link} to="/child" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/child/${childEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ChildDetail;
