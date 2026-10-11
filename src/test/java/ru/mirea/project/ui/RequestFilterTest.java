package ru.mirea.project.ui;

import org.junit.jupiter.api.Test;
import ru.mirea.project.model.RequestCategory;
import ru.mirea.project.model.RequestPriority;
import ru.mirea.project.model.RequestStatus;
import ru.mirea.project.model.ServiceRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestFilterTest {

    private final ServiceRequest printer = request(1, "Не печатает принтер", "Замятие бумаги",
            RequestCategory.HARDWARE, RequestStatus.NEW, RequestPriority.HIGH, 1);
    private final ServiceRequest vpn = request(2, "Нет доступа", "VPN не подключается",
            RequestCategory.NETWORK, RequestStatus.IN_PROGRESS, RequestPriority.MEDIUM, 3);
    private final ServiceRequest office = request(3, "Установить Office", "Нужен Word и Excel",
            RequestCategory.SOFTWARE, RequestStatus.CLOSED, RequestPriority.LOW, 2);
    private final List<ServiceRequest> all = List.of(printer, vpn, office);

    @Test
    void searchesByTitleAndDescriptionIgnoringCase() {
        assertEquals(List.of(printer), filter("ПРИНТЕР", null, null, null));
        assertEquals(List.of(vpn), filter("  vpn ", null, null, null));
        assertEquals(all, filter("", null, null, null));
    }

    @Test
    void combinesFiltersWithSearch() {
        assertEquals(List.of(vpn), filter(null, RequestStatus.IN_PROGRESS, null, null));
        assertEquals(List.of(office), filter(null, null, RequestPriority.LOW, RequestCategory.SOFTWARE));
        assertTrue(filter("принтер", RequestStatus.CLOSED, null, null).isEmpty());
    }

    @Test
    void sortsByDateAndPriority() {
        assertEquals(List.of(vpn, office, printer), sort(RequestSort.NEWEST_FIRST));
        assertEquals(List.of(printer, office, vpn), sort(RequestSort.OLDEST_FIRST));
        assertEquals(List.of(printer, vpn, office), sort(RequestSort.PRIORITY_HIGH_FIRST));
        assertEquals(List.of(office, vpn, printer), sort(RequestSort.PRIORITY_LOW_FIRST));
    }

    private List<ServiceRequest> filter(String text, RequestStatus status,
                                        RequestPriority priority, RequestCategory category) {
        return all.stream().filter(RequestFilter.of(text, status, priority, category)).toList();
    }

    private List<ServiceRequest> sort(RequestSort sort) {
        return all.stream().sorted(sort.comparator()).toList();
    }

    private static ServiceRequest request(int id, String title, String description,
                                          RequestCategory category, RequestStatus status,
                                          RequestPriority priority, int day) {
        return new ServiceRequest(id, title, description, category, status, priority, 2, null,
                LocalDateTime.of(2026, 10, day, 12, 0), null, null, null);
    }
}
