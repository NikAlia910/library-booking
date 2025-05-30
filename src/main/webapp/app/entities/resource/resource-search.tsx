import React from 'react';
import { Button, Col, Form, FormGroup, Input, Label, Row } from 'reactstrap';
import { ResourceType } from 'app/shared/model/enumerations/resource-type.model';

export interface IResourceSearchProps {
  onSearch: (searchParams: any) => void;
}

export const ResourceSearch = ({ onSearch }: IResourceSearchProps) => {
  const [searchParams, setSearchParams] = React.useState({
    title: '',
    author: '',
    keywords: '',
    resourceType: '',
  });

  const handleInputChange = event => {
    const { name, value } = event.target;
    setSearchParams(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = event => {
    event.preventDefault();
    onSearch(searchParams);
  };

  const handleReset = () => {
    setSearchParams({
      title: '',
      author: '',
      keywords: '',
      resourceType: '',
    });
    onSearch({});
  };

  return (
    <Form onSubmit={handleSubmit} className="mb-4">
      <Row>
        <Col md={3}>
          <FormGroup>
            <Label for="title">Title</Label>
            <Input
              type="text"
              name="title"
              id="title"
              value={searchParams.title}
              onChange={handleInputChange}
              placeholder="Search by title"
            />
          </FormGroup>
        </Col>
        <Col md={3}>
          <FormGroup>
            <Label for="author">Author</Label>
            <Input
              type="text"
              name="author"
              id="author"
              value={searchParams.author}
              onChange={handleInputChange}
              placeholder="Search by author"
            />
          </FormGroup>
        </Col>
        <Col md={3}>
          <FormGroup>
            <Label for="keywords">Keywords</Label>
            <Input
              type="text"
              name="keywords"
              id="keywords"
              value={searchParams.keywords}
              onChange={handleInputChange}
              placeholder="Search by keywords"
            />
          </FormGroup>
        </Col>
        <Col md={3}>
          <FormGroup>
            <Label for="resourceType">Resource Type</Label>
            <Input type="select" name="resourceType" id="resourceType" value={searchParams.resourceType} onChange={handleInputChange}>
              <option value="">All Types</option>
              {Object.keys(ResourceType).map(type => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </Input>
          </FormGroup>
        </Col>
      </Row>
      <Row>
        <Col className="text-end">
          <Button color="secondary" onClick={handleReset} className="me-2">
            Reset
          </Button>
          <Button color="primary" type="submit">
            Search
          </Button>
        </Col>
      </Row>
    </Form>
  );
};

export default ResourceSearch;
