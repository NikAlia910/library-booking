import React from 'react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { Card, CardBody, CardHeader } from 'reactstrap';
import { IReservation } from 'app/shared/model/reservation.model';
import { IResource } from 'app/shared/model/resource.model';
import dayjs from 'dayjs';

export interface IResourceCalendarProps {
  resource: IResource;
  reservations: IReservation[];
  onSelectSlot: (start: Date, end: Date) => void;
}

export const ResourceCalendar = ({ resource, reservations, onSelectSlot }: IResourceCalendarProps) => {
  const events = reservations.map(reservation => ({
    id: reservation.id.toString(),
    title: `Reserved by ${reservation.user?.login}`,
    start: dayjs(reservation.startTime).toDate(),
    end: dayjs(reservation.endTime).toDate(),
    allDay: false,
  }));

  const handleSelect = selectInfo => {
    const start = selectInfo.start;
    const end = selectInfo.end;
    onSelectSlot(start, end);
  };

  return (
    <Card className="mb-4">
      <CardHeader>
        <h4 className="mb-0">Availability Calendar - {resource.title}</h4>
      </CardHeader>
      <CardBody>
        <FullCalendar
          plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
          initialView="timeGridWeek"
          headerToolbar={{
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay',
          }}
          events={events}
          selectable={true}
          selectMirror={true}
          dayMaxEvents={true}
          weekends={true}
          select={handleSelect}
          height="auto"
          slotMinTime="08:00:00"
          slotMaxTime="20:00:00"
          allDaySlot={false}
          slotDuration="00:30:00"
          selectConstraint={{
            startTime: '08:00',
            endTime: '20:00',
            daysOfWeek: [0, 1, 2, 3, 4, 5, 6],
          }}
        />
      </CardBody>
    </Card>
  );
};

export default ResourceCalendar;
