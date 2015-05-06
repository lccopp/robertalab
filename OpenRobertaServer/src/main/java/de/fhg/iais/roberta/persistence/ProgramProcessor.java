package de.fhg.iais.roberta.persistence;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;

import de.fhg.iais.roberta.persistence.bo.AccessRight;
import de.fhg.iais.roberta.persistence.bo.Program;
import de.fhg.iais.roberta.persistence.bo.Relation;
import de.fhg.iais.roberta.persistence.bo.User;
import de.fhg.iais.roberta.persistence.dao.AccessRightDao;
import de.fhg.iais.roberta.persistence.dao.ProgramDao;
import de.fhg.iais.roberta.persistence.dao.UserDao;
import de.fhg.iais.roberta.persistence.util.DbSession;
import de.fhg.iais.roberta.persistence.util.HttpSessionState;
import de.fhg.iais.roberta.util.Key;
import de.fhg.iais.roberta.util.Util;

public class ProgramProcessor extends AbstractProcessor {
    public ProgramProcessor(DbSession dbSession, HttpSessionState httpSessionState) {
        super(dbSession, httpSessionState);
    }

    public Program getProgram(String programName, int ownerId) {
        if ( !Util.isValidJavaIdentifier(programName) ) {
            setError(Key.PROGRAM_ERROR_ID_INVALID, programName);
            return null;
        } else if ( this.httpSessionState.isUserLoggedIn() ) {
            UserDao userDao = new UserDao(this.dbSession);
            ProgramDao programDao = new ProgramDao(this.dbSession);
            User owner = userDao.get(ownerId);
            Program program = programDao.load(programName, owner);
            if ( program != null ) {
                setSuccess(Key.PROGRAM_GET_ONE_SUCCESS);
                return program;
            } else {
                program = getProgramWithAccessRight(programName, ownerId);
                if ( program != null ) {
                    setSuccess(Key.PROGRAM_GET_ONE_SUCCESS);
                    return program;
                } else {
                    setError(Key.PROGRAM_GET_ONE_ERROR_NOT_FOUND);
                    return null;
                }
            }
        } else {
            setError(Key.PROGRAM_GET_ONE_ERROR_NOT_LOGGED_IN);
            return null;
        }
    }

    public JSONArray getProgramInfo(int ownerId) {
        UserDao userDao = new UserDao(this.dbSession);
        ProgramDao programDao = new ProgramDao(this.dbSession);
        AccessRightDao accessRightDao = new AccessRightDao(this.dbSession);
        User owner = userDao.get(ownerId);
        // First we obtain all programs owned by the user
        List<Program> programs = programDao.loadAll(owner);
        JSONArray programInfos = new JSONArray();
        for ( Program program : programs ) {
            JSONArray programInfo = new JSONArray();
            programInfo.put(program.getName());
            programInfo.put(program.getOwner().getAccount());
            //            programInfo.put(program.getNumberOfBlocks());

            //If shared find with whom and under which rights
            List<AccessRight> accessRights = accessRightDao.loadAccessRightsByProgram(program);
            String sharedWithUser = null;
            String rights = null;
            for ( AccessRight accessRight : accessRights ) {
                if ( sharedWithUser != null ) {
                    sharedWithUser += "<br>" + accessRight.getUser().getAccount();
                } else {
                    sharedWithUser = accessRight.getUser().getAccount();
                }
                if ( rights != null ) {
                    rights += "<br>" + accessRight.getRelation().toString();
                } else {
                    rights = accessRight.getRelation().toString();
                }
            }

            programInfo.put(sharedWithUser);
            programInfo.put(rights);
            programInfo.put(program.getCreated().toString());
            programInfo.put(program.getLastChanged().toString());
            programInfos.put(programInfo);
        }
        // Now we find all the programs which are not owned by the user but have been shared to him
        List<AccessRight> accessRights2 = accessRightDao.loadAccessRightsForUser(owner);
        for ( AccessRight accessRight : accessRights2 ) {
            JSONArray programInfo2 = new JSONArray();
            programInfo2.put(accessRight.getProgram().getName());
            programInfo2.put(accessRight.getProgram().getOwner().getAccount());
            //            programInfo2.put(userProgram.getProgram().getNumberOfBlocks());
            programInfo2.put(owner.getAccount());
            programInfo2.put(accessRight.getRelation().toString());
            programInfo2.put(accessRight.getProgram().getCreated().toString());
            programInfo2.put(accessRight.getProgram().getLastChanged().toString());
            programInfos.put(programInfo2);
        }

        setSuccess(Key.PROGRAM_GET_ALL_SUCCESS, "" + programInfos.length());
        return programInfos;
    }

    /**
     * Test if a given user has write or read access rights for a given program that was created by another user
     *
     * @param programName the name of the program
     * @param ownerId the owner of the program
     */
    private Program getProgramWithAccessRight(String programName, int ownerId) {
        UserDao userDao = new UserDao(this.dbSession);
        AccessRightDao accessRightDao = new AccessRightDao(this.dbSession);
        User owner = userDao.get(ownerId);

        // Find all the programs which are not owned by the user but have been shared to him
        List<AccessRight> accessRights = accessRightDao.loadAccessRightsForUser(owner);
        for ( AccessRight accessRight : accessRights ) {
            Program program = accessRight.getProgram();
            String userProgramName = program.getName();
            if ( programName.equals(userProgramName) ) {
                String relation = accessRight.getRelation().toString();
                if ( relation.equals(Relation.READ.toString()) || relation.equals(Relation.WRITE.toString()) ) {
                    return program;
                }
            }
        }
        return null;
    }

    /**
     * update a given program owned by a given user. Overwrites an existing program if mayExist == true.
     *
     * @param programName the name of the program
     * @param userId the owner of the program
     * @param programText the new program text
     * @param mayExist true, if an existing program may be changed; false if a program may be stored only, if it does not exist in the database
     * @param isOwner true, if the owner updates a program; false if a user with access right WRITE updates a program
     */
    public void updateProgram(String programName, int userId, String programText, boolean mayExist, boolean isOwner) {
        if ( !Util.isValidJavaIdentifier(programName) ) {
            setError(Key.PROGRAM_ERROR_ID_INVALID, programName);
            return;
        }
        if ( this.httpSessionState.isUserLoggedIn() ) {
            UserDao userDao = new UserDao(this.dbSession);
            ProgramDao programDao = new ProgramDao(this.dbSession);
            User user = userDao.get(userId);
            boolean success = programDao.persistProgramText(programName, user, programText, mayExist, isOwner);
            if ( success ) {
                setSuccess(Key.PROGRAM_SAVE_SUCCESS);
            } else {
                setError(Key.PROGRAM_SAVE_ERROR_NOT_SAVED_TO_DB);
            }
        } else {
            setError(Key.USER_ERROR_NOT_LOGGED_IN);
        }
    }

    public void deleteByName(String programName, int ownerId) {
        UserDao userDao = new UserDao(this.dbSession);
        ProgramDao programDao = new ProgramDao(this.dbSession);
        User owner = userDao.get(ownerId);
        int rowCount = programDao.deleteByName(programName, owner);
        if ( rowCount > 0 ) {
            setSuccess(Key.PROGRAM_DELETE_SUCCESS);
        } else {
            setError(Key.PROGRAM_DELETE_ERROR);
        }
    }
}
