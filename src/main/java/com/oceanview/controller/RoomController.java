package com.oceanview.controller;

import com.oceanview.model.Room;
import com.oceanview.service.RoomService;
import com.oceanview.service.ServiceException;
import com.oceanview.service.impl.RoomServiceImpl;
import com.oceanview.util.ServiceResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@WebServlet("/rooms")
public class RoomController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private RoomService roomService;

    @Override
    public void init() throws ServletException {
        roomService = new RoomServiceImpl();
        log.info("RoomController initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) action = "list";
        log.debug("GET /rooms action={}", action);
        try {
            switch (action) {
                case "list": handleList(req, resp); break;
                case "new": handleNewForm(req, resp); break;
                case "edit": handleEditForm(req, resp); break;
                case "view": handleView(req, resp); break;
                case "rate": handleRate(req, resp); break;
                default:
                    log.warn("Unknown GET action '{}' for /rooms — falling back to list", action);
                    handleList(req, resp);
            }
        } catch (ServiceException e) {
            log.error("ServiceException in GET /rooms action='{}': {}", action, e.getMessage(), e);
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/room/list.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validateCsrf(req, resp)) return;
        String action = req.getParameter("action");
        if (action == null) action = "create";
        log.debug("POST /rooms action={}", action);
        try {
            switch (action) {
                case "create": handleCreate(req, resp); break;
                case "update": handleUpdate(req, resp); break;
                case "delete": handleDelete(req, resp); break;
                default:
                    log.warn("Unknown POST action '{}' for /rooms — falling back to list", action);
                    handleList(req, resp);
            }
        } catch (ServiceException e) {
            log.error("ServiceException in POST /rooms action='{}': {}", action, e.getMessage(), e);
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/room/list.jsp").forward(req, resp);
        }
    }

    private void handleList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String roomType = req.getParameter("roomType");
        String floorStr = req.getParameter("floor");
        String checkInStr = req.getParameter("checkIn");
        String checkOutStr = req.getParameter("checkOut");

        Integer floor = (floorStr != null && !floorStr.isEmpty()) ? Integer.parseInt(floorStr) : null;
        LocalDate checkIn = (checkInStr != null && !checkInStr.isEmpty()) ? LocalDate.parse(checkInStr) : null;
        LocalDate checkOut = (checkOutStr != null && !checkOutStr.isEmpty()) ? LocalDate.parse(checkOutStr) : null;

        log.debug("handleList: roomType={} floor={} checkIn={} checkOut={}", roomType, floor, checkIn, checkOut);
        List<Room> rooms;
        if (roomType != null || floor != null || checkIn != null) {
            rooms = roomService.getRoomsByFilters(roomType, floor, checkIn, checkOut);
        } else {
            rooms = roomService.getAllRooms();
        }
        log.debug("handleList: loaded {} room(s)", rooms.size());
        req.setAttribute("rooms", rooms);
        req.getRequestDispatcher("/WEB-INF/views/room/list.jsp").forward(req, resp);
    }

    private void handleView(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String id = req.getParameter("id");
        log.debug("handleView: id={}", id);
        Room room = roomService.getRoom(id);
        if (room == null) {
            log.warn("handleView: room not found for id={}", id);
            resp.sendError(404, "Room not found");
            return;
        }
        req.setAttribute("room", room);
        req.getRequestDispatcher("/WEB-INF/views/room/edit.jsp").forward(req, resp);
    }

    private void handleNewForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("handleNewForm: showing add room form");
        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/room/add.jsp").forward(req, resp);
    }

    private void handleEditForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String id = req.getParameter("id");
        log.debug("handleEditForm: id={}", id);
        Room room = roomService.getRoom(id);
        if (room == null) {
            log.warn("handleEditForm: room not found for id={}", id);
            resp.sendError(404, "Room not found");
            return;
        }
        req.setAttribute("room", room);
        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/room/edit.jsp").forward(req, resp);
    }

    private void handleRate(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServiceException {
        String roomId = req.getParameter("id");
        log.debug("handleRate: roomId={}", roomId);
        Room room = roomService.getRoom(roomId);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"rate\":" + (room != null ? room.getRatePerNight() : 0) + "}");
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        Room room = new Room();
        room.setRoomNumber(req.getParameter("roomNumber"));
        room.setRoomType(req.getParameter("roomType"));
        room.setFloor(parseInt(req.getParameter("floor"), 1));
        room.setCapacity(parseInt(req.getParameter("capacity"), 2));
        room.setRatePerNight(parseDouble(req.getParameter("ratePerNight"), 0));
        room.setDescription(req.getParameter("description"));
        String[] amenityArr = req.getParameterValues("amenities");
        room.setAmenities(amenityArr != null ? String.join(",", amenityArr) : "");
        room.setImageUrl(req.getParameter("imageUrl"));

        log.info("Creating room: number={} type={} floor={} capacity={} rate={}", room.getRoomNumber(), room.getRoomType(), room.getFloor(), room.getCapacity(), room.getRatePerNight());
        ServiceResult result = roomService.addRoom(room);
        if (result.isSuccess()) {
            log.info("Room created: number={}", room.getRoomNumber());
            req.getSession().setAttribute("flashSuccess", result.getMessage());
            resp.sendRedirect(req.getContextPath() + "/rooms?action=list");
        } else {
            log.warn("Room creation failed for number='{}': {}", room.getRoomNumber(), result.getMessage());
            req.setAttribute("error", result.getMessage());
            req.setAttribute("room", room);
            generateCsrfToken(req);
            req.getRequestDispatcher("/WEB-INF/views/room/add.jsp").forward(req, resp);
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        Room room = new Room();
        room.setId(req.getParameter("id"));
        room.setRoomNumber(req.getParameter("roomNumber"));
        room.setRoomType(req.getParameter("roomType"));
        room.setFloor(parseInt(req.getParameter("floor"), 1));
        room.setCapacity(parseInt(req.getParameter("capacity"), 2));
        room.setRatePerNight(parseDouble(req.getParameter("ratePerNight"), 0));
        room.setAvailable("true".equals(req.getParameter("isAvailable")) || "on".equals(req.getParameter("isAvailable")));
        room.setDescription(req.getParameter("description"));
        String[] amenityArrU = req.getParameterValues("amenities");
        room.setAmenities(amenityArrU != null ? String.join(",", amenityArrU) : "");
        room.setImageUrl(req.getParameter("imageUrl"));

        log.info("Updating room id={} number={} type={}", room.getId(), room.getRoomNumber(), room.getRoomType());
        ServiceResult result = roomService.updateRoom(room);
        if (result.isSuccess()) {
            log.info("Room updated successfully: id={}", room.getId());
            req.getSession().setAttribute("flashSuccess", result.getMessage());
            resp.sendRedirect(req.getContextPath() + "/rooms?action=list");
        } else {
            log.warn("Room update failed for id='{}': {}", room.getId(), result.getMessage());
            req.setAttribute("error", result.getMessage());
            req.setAttribute("room", room);
            generateCsrfToken(req);
            req.getRequestDispatcher("/WEB-INF/views/room/edit.jsp").forward(req, resp);
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServiceException {
        String id = req.getParameter("id");
        log.info("Deleting room id={}", id);
        ServiceResult result = roomService.deleteRoom(id);
        if (result.isSuccess()) {
            log.info("Room deleted: id={}", id);
            req.getSession().setAttribute("flashSuccess", result.getMessage());
        } else {
            log.warn("Room delete failed for id='{}': {}", id, result.getMessage());
            req.getSession().setAttribute("flashError", result.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/rooms?action=list");
    }

    private void generateCsrfToken(HttpServletRequest req) {
        String token = UUID.randomUUID().toString();
        req.getSession().setAttribute("csrfToken", token);
        req.setAttribute("csrfToken", token);
    }

    private boolean validateCsrf(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String formToken = req.getParameter("csrfToken");
        String sessionToken = (String) req.getSession().getAttribute("csrfToken");
        if (formToken == null || !formToken.equals(sessionToken)) {
            resp.sendError(403, "CSRF validation failed"); return false;
        }
        return true;
    }

    private int parseInt(String val, int def) {
        try { return val != null ? Integer.parseInt(val) : def; } catch (NumberFormatException e) { return def; }
    }

    private double parseDouble(String val, double def) {
        try { return val != null ? Double.parseDouble(val) : def; } catch (NumberFormatException e) { return def; }
    }
}

