/*
 * Copyright (c) 2009-2010 Chris Smith
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.md87.charliebravo.commands;

import com.dmdirc.util.DateUtils;
import com.md87.charliebravo.Command;
import com.md87.charliebravo.CommandOptions;
import com.md87.charliebravo.ConfigCache;
import com.md87.charliebravo.Followup;
import com.md87.charliebravo.InputHandler;
import com.md87.charliebravo.Response;
import uk.co.md87.evetool.api.ApiResponse;
import uk.co.md87.evetool.api.EveApi;
import uk.co.md87.evetool.api.wrappers.SkillInTraining;
import uk.co.md87.evetool.api.wrappers.SkillList;

/**
 *
 * @author chris
 */
@CommandOptions(requireAuthorisation=true, requiredSettings={"eve.apikey","eve.userid","eve.charid"})
public class SkillCommand implements Command {

    public void execute(InputHandler handler, Response response, String line) throws Exception {
        final String openID = (String) handler.getParser().getClient(response.getSource())
                .getMap().get("OpenID");

        if (openID == null) {
            response.sendMessage("You must be authorised to use this command", true);
        } else {
            final EveApi api = new EveApi(new ConfigCache(handler.getConfig().getConfigfile()));
            api.setApiKey(handler.getConfig().getOption(openID, "eve.apikey"));
            api.setUserID(Integer.parseInt(handler.getConfig().getOption(openID, "eve.userid")));
            api.setCharID(Integer.parseInt(handler.getConfig().getOption(openID, "eve.charid")));

            final ApiResponse<SkillInTraining> res = api.getSkillInTraining();

            if (res.wasSuccessful()) {
                final SkillInTraining skill = res.getResult();

                final ApiResponse<SkillList> res2 = api.getSkillTree();

                if (res2.wasSuccessful()) {
                    if (skill.isInTraining() && System.currentTimeMillis()
                                < skill.getEndTime().getTime() - 1000) {
                        skill.setSkill(res2.getResult().getSkillById(skill.getTypeId()));
                        response.sendMessage("you are currently training "
                                + skill.getSkill().getName() + " to level "
                                + skill.getTargetLevel() + ". It will finish in "
                                + DateUtils.formatDuration((int) (skill.getEndTime()
                                .getTime() - System.currentTimeMillis()) / 1000));
                    } else {
                        response.sendMessage("You are not training anything", true);
                    }
                } else {
                    response.sendMessage("There was an error retrieving the EVE skill list", true);
                    response.addFollowup(new ErrorFollowup(res2));
                    response.addFollowup(new RetryFollowup(this));
                }
            } else {
                response.sendMessage("There was an error retrieving your skill information", true);
                response.addFollowup(new ErrorFollowup(res));
                response.addFollowup(new RetryFollowup(this));
            }

            response.addFollowup(new CacheFollowup(res));
        }
    }

    protected static class CacheFollowup implements Followup {

        protected final ApiResponse<?> apiresponse;

        public CacheFollowup(ApiResponse<?> apiresponse) {
            this.apiresponse = apiresponse;
        }

        public boolean matches(String line) {
            return line.equalsIgnoreCase("cache");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            response.setInheritFollows(true);
            response.sendMessage("the result has been cached for " +
                    DateUtils.formatDuration((int) (System.currentTimeMillis()
                    - apiresponse.getApiResult().getCachedSince().getTime()) / 1000)
                    + ", and will expire in " +
                    DateUtils.formatDuration((int) (apiresponse.getApiResult()
                    .getCachedUntil().getTime() - System.currentTimeMillis()) / 1000));
        }

    }

    protected static class ErrorFollowup implements Followup {

        protected final ApiResponse<?> apiresponse;

        public ErrorFollowup(ApiResponse<?> response) {
            this.apiresponse = response;
        }

        public boolean matches(String line) {
            return line.equalsIgnoreCase("error");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            response.setInheritFollows(true);
            response.sendMessage("the error message was: " + apiresponse.getError());
        }

    }

    protected static class RetryFollowup implements Followup {

        protected final Command command;

        public RetryFollowup(Command command) {
            this.command = command;
        }

        public boolean matches(String line) {
            return line.equalsIgnoreCase("retry");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            command.execute(handler, response, line);
        }

    }

}
